# Plan de tests — EducManager

> [Benjamin – #06, 2026-10-15] Création. Les résultats d'exécution sont en §5 (mis à jour à chaque campagne).

## 1. Stratégie

| Niveau | Objectif | Outils | Où |
|---|---|---|---|
| Tests unitaires backend | Règles métier isolées (services, calcul de planning, JWT, anti force brute, audit) | JUnit 5, Mockito, AssertJ | `backend/src/test/java/.../service`, `security`, `audit` |
| Tests de la couche web | Contrôleurs (sérialisation, codes HTTP) | MockMvc (`@WebMvcTest`, standalone) | `.../controller` (équipe phase 1) |
| Tests d'accès aux données | Requêtes JPA | `@DataJpaTest` (H2) | `.../repository` (équipe phase 1) |
| Tests d'intégration | Application complète : sécurité + contrôleurs + services + base réelle | `@SpringBootTest` + MockMvc, sur H2 **et** PostgreSQL 16 ; MongoDB réel en CI | `.../integration` |
| Tests unitaires frontend | Session, intercepteur, guards, composants | Jasmine, Karma (Chrome sans interface), `HttpTestingController` | `frontend/src/**/*.spec.ts` |
| Test de fumée (déploiement) | La stack Docker déployée répond de bout en bout | `scripts/smoke-test.sh` (curl) | CI job `docker` |
| Recette manuelle | Parcours utilisateur par rôle dans le navigateur | Navigateur | §4 |

**Critères de sortie :** 100 % des tests automatisés au vert dans la CI (backend, frontend, Docker) avant fusion dans `main`.

## 2. Cas de tests automatisés — backend

| ID | Exigence | Cas | Résultat attendu | Test |
|---|---|---|---|---|
| T-B01 | US01 | Connexion avec identifiants valides (email en majuscules) | 200, token + rôle + prénom | `SecurityIntegrationTest.loginRenvoieUnTokenEtLeRole` |
| T-B02 | US01 | Mauvais mot de passe | 401, message générique | `…mauvaisMotDePasseRenvoie401AvecMessageGenerique` |
| T-B03 | US01 | 6ᵉ tentative après 5 échecs | 429 ; déblocage après 15 min ; remise à zéro après succès | `…bloqueApresCinqEchecs`, `LoginAttemptServiceTest` (3 cas) |
| T-B04 | US02 | Appel API sans token / avec faux token | 401 | `…sansTokenOuAvecUnFauxTokenRenvoie401` |
| T-B05 | US02 | Token expiré, modifié, signé avec un autre secret ; secret trop court | refusé / exception au démarrage | `JwtUtilsTest` (5 cas) |
| T-B06 | US01 | `GET /api/auth/me` | utilisateur du token | `…meRenvoieLUtilisateurDuToken` |
| T-B07 | US10 | Élève / formateur tentent d'écrire dans le référentiel | 403 (lecture 200) | `…unEtudiantPeutLireMaisPasModifierLeReferentiel` |
| T-B08 | US07 | Élève : liste des élèves, toutes les inscriptions | 403 ; `/enrollment/me` = ses inscriptions | `…unEtudiantNeVoitQueSesPropresDonnees` |
| T-B09 | US08/09 | Référente / élève sur `/api/users`, `/api/audit` | 403 ; admin 200 | `…seulLAdminGereLesComptesEtConsulteLAudit` |
| T-B10 | US03 | Créer (code normalisé en majuscules), modifier, supprimer un cours ; code en double | 200/200/204 ; 409 | `…coursCrudEtSuppressionInterditeSiUtilise` |
| T-B11 | US03 | Supprimer un cours utilisé dans un cursus | 409 | idem |
| T-B12 | US05 | Créer une promotion valide | créée, nom nettoyé | `PromotionServiceTest.creeUnePromotionValide` |
| T-B13 | US05 | Date de fin avant date de début | 400, rien enregistré | `PromotionServiceTest.refuseUneDateDeFin…`, `…validationDesDonnees…` |
| T-B14 | US05 | Nom en double ; cursus inexistant ; suppression avec inscrits | 409 ; 404 ; 409 | `PromotionServiceTest` (3 cas), parcours d'intégration |
| T-B15 | US06 | Inscrire à une promotion / à un cours | 201, date du jour | `EnrollmentServiceTest` (2 cas) |
| T-B16 | US06 | Double inscription | 409 | `EnrollmentServiceTest.refuseUneDoubleInscription`, parcours d'intégration |
| T-B17 | US06 | Promotion terminée sans / avec « forcer » ; type incohérent | 409 / 201 ; 400 | `EnrollmentServiceTest` (2 cas) |
| T-B18 | US07 | Planning : enchaînement en jours ouvrés, début un samedi, durée absente | dates attendues | `PlanningCalculatorTest` (4 cas), parcours d'intégration |
| T-B19 | US08 | Créer un compte élève | mot de passe haché BCrypt, fiche élève créée, email en minuscules | `UserServiceTest.creeUnCompteEtudiant…` |
| T-B20 | US08 | Email déjà utilisé | 409 | `UserServiceTest.refuseUnEmailDejaUtilise` |
| T-B21 | US08 | Admin supprime / rétrograde son propre compte ; élève avec inscriptions ; mot de passe vide | 400 ; 409 ; inchangé | `UserServiceTest` (3 cas) |
| T-B22 | US09 | Connexions échouée puis réussie | 2 documents dans MongoDB | `AuditMongoIntegrationTest` (CI) |
| T-B23 | US09 | MongoDB en panne ; audit désactivé ; pagination hors bornes | aucune erreur remontée ; rien écrit ; taille ≤ 100 | `AuditServiceTest` (5 cas) |
| T-B24 | toutes | Données invalides (email, mot de passe, champ vide) | 400 avec détail par champ | `…validationDesDonneesRenvoie400AvecLeDetailDesChamps` |
| T-B25 | US05/06/08 | Parcours complet : admin crée un élève → référente crée une promotion → inscrit → doublon refusé → l'élève voit son planning → suppression de promotion refusée | 201/201/201/409/200/409 | `…parcoursReferente_creerPromotionInscrireEleveEtVoirLePlanning` |

Les tests de l'équipe (phase 1) restent en place : filières, cursus, cours, association cursus-cours (services, contrôleurs, repositories, mappers).

## 3. Cas de tests automatisés — frontend

| ID | Cas | Test |
|---|---|---|
| T-F01 | Login : appel `/api/auth/login`, stockage du token, rôle, page par défaut ; 401 propagé | `auth.service.spec.ts` |
| T-F02 | Token expiré = session terminée ; déconnexion efface la session | `auth.service.spec.ts` |
| T-F03 | Intercepteur : ajoute `Bearer` sur `/api`, pas sur un site externe ; 401 → déconnexion + `/login` (sauf sur le login) | `auth.interceptor.spec.ts` |
| T-F04 | Cours : affichage des données de l'API ; formateur en lecture seule ; recherche ; formulaire incomplet refusé | `cours-list.component.spec.ts` |
| T-F05 | Promotions : dates inversées bloquées sans appel API ; création valide | `promotion-list.component.spec.ts` |
| T-F06 | Utilisateurs : règle de mot de passe identique au backend ; mot de passe vide accepté en modification ; compte courant reconnu | `user-list.component.spec.ts` |
| T-F07 | Guard de rôle : accès autorisé / redirection | `role.guard.spec.ts` |
| T-F08 | Messages d'erreur API (champ, 403, serveur injoignable) | `api-error.spec.ts` |

## 4. Recette manuelle (navigateur)

| ID | Rôle | Scénario | Résultat attendu |
|---|---|---|---|
| T-M01 | tous | Se connecter avec chaque compte de démonstration | Menu limité au rôle, page par défaut correcte |
| T-M02 | REFERENTE | Créer une promotion avec dates inversées | Bouton désactivé + message |
| T-M03 | REFERENTE | Inscrire l'élève de démo à « DWWM 2025 » sans puis avec « Forcer » | Message d'erreur puis confirmation |
| T-M04 | REFERENTE | Cursus → « Cours » → ajouter / retirer un cours | Liste ordonnée mise à jour |
| T-M05 | TEACHER | Ouvrir « Cours » | Aucun bouton de modification |
| T-M06 | STUDENT | Ouvrir « Mon planning » | Cours de CDA 2026-2027 datés à partir du 07/09/2026 |
| T-M07 | ADMIN | Créer un compte élève avec un mot de passe trop court | Message ; puis création OK |
| T-M08 | ADMIN | Journal d'audit, filtre `LOGIN_FAILURE` | Les échecs de connexion apparaissent (avec MongoDB lancé) |

## 5. Résultats

| Campagne | Environnement | Résultat |
|---|---|---|
| 15/10/2026 | Backend, H2 (`mvn test`, Java 21) | **106 tests : 105 réussis, 0 échec**, 1 ignoré (test MongoDB, pas de MongoDB sur le poste) |
| 15/10/2026 | Backend, tests d'intégration sur **PostgreSQL 16** (profil prod) | **13 / 13 réussis** (`SecurityIntegrationTest` + chargement du contexte) |
| 15/10/2026 | Frontend (Karma, Chrome sans interface) | **24 / 24 réussis** — couverture lignes ≈ 58 % |
| 15/10/2026 | Build Angular de production | OK |
| CI GitHub Actions | Backend (PostgreSQL + MongoDB), frontend, Docker + test de fumée | voir l'onglet *Actions* du dépôt et `readme.txt` |
