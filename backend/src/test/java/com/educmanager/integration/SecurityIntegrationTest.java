package com.educmanager.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de bout en bout : application complète (sécurité, contrôleurs, services,
 * JPA) avec les données de démonstration de DataInitializer.
 * Base : H2 en local ; PostgreSQL dans la CI (SPRING_PROFILES_ACTIVE=prod, voir .github/workflows/ci.yml).
 *
 * <p>[Benjamin – #06, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private String login(String email, String password) throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + JsonPath.read(res.getResponse().getContentAsString(), "$.token");
    }

    private String admin() throws Exception { return login("admin@educmanager.fr", "admin123"); }
    private String referente() throws Exception { return login("referente@educmanager.fr", "referente123"); }
    private String etudiant() throws Exception { return login("etudiant@educmanager.fr", "etudiant123"); }
    private String formateur() throws Exception { return login("formateur@educmanager.fr", "formateur123"); }

    // ---------- Authentification ----------

    @Test
    void loginRenvoieUnTokenEtLeRole() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ADMIN@educmanager.fr\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void mauvaisMotDePasseRenvoie401AvecMessageGenerique() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin@educmanager.fr\",\"password\":\"faux\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect."));
    }

    @Test
    void bloqueApresCinqEchecs() throws Exception {
        String body = "{\"username\":\"brute-force@test.fr\",\"password\":\"x\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void sansTokenOuAvecUnFauxTokenRenvoie401() throws Exception {
        mvc.perform(get("/api/filieres")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/filieres").header("Authorization", "Bearer faux.token.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEstPublicEtIndiqueUp() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void meRenvoieLUtilisateurDuToken() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", etudiant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("etudiant@educmanager.fr"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    // ---------- Droits par rôle ----------

    @Test
    void unEtudiantPeutLireMaisPasModifierLeReferentiel() throws Exception {
        String token = etudiant();
        mvc.perform(get("/api/filieres").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/filieres").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Pirate\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/courses").header("Authorization", formateur())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"X\",\"titre\":\"X\",\"dureeJours\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void seulLAdminGereLesComptesEtConsulteLAudit() throws Exception {
        mvc.perform(get("/api/users").header("Authorization", referente())).andExpect(status().isForbidden());
        mvc.perform(get("/api/audit").header("Authorization", etudiant())).andExpect(status().isForbidden());
        mvc.perform(get("/api/users").header("Authorization", admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(4)));
    }

    @Test
    void unEtudiantNeVoitQueSesPropresDonnees() throws Exception {
        mvc.perform(get("/api/student").header("Authorization", etudiant())).andExpect(status().isForbidden());
        mvc.perform(get("/api/enrollment").header("Authorization", etudiant())).andExpect(status().isForbidden());
        mvc.perform(get("/api/enrollment/me").header("Authorization", etudiant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promotionName").value("CDA 2026-2027"));
    }

    // ---------- Parcours métier ----------

    @Test
    void parcoursReferente_creerPromotionInscrireEleveEtVoirLePlanning() throws Exception {
        String admin = admin();
        String ref = referente();

        // 1. l'admin crée un compte élève
        MvcResult user = mvc.perform(post("/api/users").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"it.eleve@test.fr","firstName":"Ines","lastName":"Test",
                                 "role":"STUDENT","password":"motdepasse1"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").isNumber())
                .andReturn();
        Integer studentId = JsonPath.read(user.getResponse().getContentAsString(), "$.studentId");

        // 2. la référente crée une promotion du cursus CDA (id du cursus lu dans l'API)
        MvcResult cursus = mvc.perform(get("/api/cursus").header("Authorization", ref)).andReturn();
        Integer cursusId = JsonPath.read(cursus.getResponse().getContentAsString(), "$[0].id");
        MvcResult promo = mvc.perform(post("/api/promotions").header("Authorization", ref)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"IT 2030\",\"startDate\":\"2030-01-07\",\"endDate\":\"2030-06-28\",\"cursusId\":" + cursusId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        Integer promoId = JsonPath.read(promo.getResponse().getContentAsString(), "$.id");

        // 3. inscription, puis doublon refusé
        String enrollment = "{\"studentId\":" + studentId + ",\"type\":\"PROMOTION\",\"promotionId\":" + promoId + "}";
        mvc.perform(post("/api/enrollment").header("Authorization", ref)
                        .contentType(MediaType.APPLICATION_JSON).content(enrollment))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/enrollment").header("Authorization", ref)
                        .contentType(MediaType.APPLICATION_JSON).content(enrollment))
                .andExpect(status().isConflict());

        // 4. l'élève voit son planning calculé
        mvc.perform(get("/api/enrollment/me/planning").header("Authorization", login("it.eleve@test.fr", "motdepasse1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].promotionName").value("IT 2030"))
                .andExpect(jsonPath("$[0].startDate").value("2030-01-07"));

        // 5. suppression d'une promotion avec inscrits refusée
        mvc.perform(delete("/api/promotions/" + promoId).header("Authorization", ref))
                .andExpect(status().isConflict());
    }

    @Test
    void validationDesDonneesRenvoie400AvecLeDetailDesChamps() throws Exception {
        mvc.perform(post("/api/users").header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"pas-un-email\",\"firstName\":\"\",\"lastName\":\"X\",\"role\":\"STUDENT\",\"password\":\"court\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.password").exists());

        mvc.perform(post("/api/promotions").header("Authorization", referente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dates inversées\",\"startDate\":\"2030-06-01\",\"endDate\":\"2030-01-01\",\"cursusId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void coursCrudEtSuppressionInterditeSiUtilise() throws Exception {
        String ref = referente();
        MvcResult created = mvc.perform(post("/api/courses").header("Authorization", ref)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"it-01\",\"titre\":\"Cours IT\",\"dureeJours\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IT-01"))
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mvc.perform(post("/api/courses").header("Authorization", ref)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"IT-01\",\"titre\":\"Doublon\",\"dureeJours\":2}"))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/courses/" + id).header("Authorization", ref)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"IT-01\",\"titre\":\"Cours IT modifié\",\"dureeJours\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Cours IT modifié"));

        mvc.perform(delete("/api/courses/" + id).header("Authorization", ref)).andExpect(status().isNoContent());

        // JAVA-01 est utilisé dans le cursus CDA (données de démo)
        MvcResult all = mvc.perform(get("/api/courses").header("Authorization", ref)).andReturn();
        java.util.List<Integer> javaIds = JsonPath.read(all.getResponse().getContentAsString(), "$[?(@.code=='JAVA-01')].id");
        Integer javaId = javaIds.get(0);
        mvc.perform(delete("/api/courses/" + javaId).header("Authorization", ref)).andExpect(status().isConflict());
    }

    @Test
    void lesRolesDesComptesDeDemoSontCorrects() throws Exception {
        mvc.perform(get("/api/users").header("Authorization", admin()))
                .andExpect(jsonPath("$[*].role", hasItem("TEACHER")))
                .andExpect(jsonPath("$[*].role", hasItem("REFERENTE")));
    }
}
