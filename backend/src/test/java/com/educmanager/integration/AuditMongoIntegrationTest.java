package com.educmanager.integration;

import com.educmanager.audit.AuditLog;
import com.educmanager.audit.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie l'écriture réelle du journal d'audit dans MongoDB.
 * Exécuté seulement si une base MongoDB est disponible (variable MONGO_IT=true, cas de la CI GitHub Actions).
 *
 * <p>[Benjamin – #06, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "MONGO_IT", matches = "true")
class AuditMongoIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuditLogRepository repository;

    @Test
    void lesConnexionsSontJournaliseesDansMongo() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"referente@educmanager.fr\",\"password\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"referente@educmanager.fr\",\"password\":\"referente123\"}"))
                .andExpect(status().isOk());

        // l'écriture est asynchrone : on attend jusqu'à 10 s
        List<AuditLog> logs = List.of();
        for (int i = 0; i < 50 && logs.size() < 2; i++) {
            Thread.sleep(200);
            logs = repository.findByUsernameOrderByTimestampDesc("referente@educmanager.fr");
        }

        assertThat(logs).extracting(AuditLog::getAction).contains("LOGIN_FAILURE", "LOGIN_SUCCESS");
        assertThat(logs).allSatisfy(log -> assertThat(log.getTimestamp()).isNotNull());
    }
}
