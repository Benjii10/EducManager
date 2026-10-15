# Journal de développement — EducManager

Une entrée par lot de travail (numéro `#NN` repris dans les commentaires du code).
Pour chaque lot : objectif, ce qui a été fait, fichiers touchés, comment vérifier, ce qui reste.

> Lots #00 à #10 : **Benjamin**. Vue d'ensemble et décisions : `readme.txt` à la racine.

---

## #02 – Conception : cahier des charges, maquettes, modèle de données, architecture

- **Date :** 15/10/2026 — **Compétences CDA :** analyser les besoins et maquetter ; définir l'architecture ; concevoir la base
- `docs/conception/CAHIER_DES_CHARGES.md` : contexte, 4 rôles, périmètre, **10 user stories avec critères d'acceptation** reliés aux tests, exigences non fonctionnelles.
- `docs/conception/maquettes/*.svg` : 6 wireframes (connexion, promotions, inscriptions, utilisateurs, planning, audit) + principes d'ergonomie et enchaînement des écrans.
- `docs/conception/MODELE_DE_DONNEES.md` : MCD (Mermaid), MLD, règles de gestion, justification de MongoDB pour l'audit, format du document.
- `docs/conception/schema-postgresql.sql` : schéma physique **extrait de la vraie base PostgreSQL 16** (pg_dump) créée par l'application.
- `docs/conception/ARCHITECTURE.md` : déploiement, couches backend, structure Angular, choix techniques justifiés.

## #03 – Fonctionnalités métier (backend)

- **Date :** 15/10/2026 — **Compétences :** composants métier, accès aux données SQL
- **Nouvelles entités / tables :** `Promotion` ; `Enrollment` refait avec de vraies clés étrangères vers `Promotion` et `Course` (avant : identifiants sans table).
- **Nouveaux services :** `PromotionService`, `EnrollmentService` (règles déplacées hors du contrôleur), `UserService`, `PlanningCalculator` (dates en jours ouvrés), `LoginAttemptService`.
- **Cours :** lecture par id, modification, suppression (interdite si utilisé), code unique ; contrat GET/POST de Manar conservé (ses tests passent sans modification).
- **API ajoutées :** `/api/promotions` (CRUD), `/api/enrollment` (liste, inscription, suppression, `/me`, `/me/planning`), `/api/users` (ADMIN), `/api/courses/{id}` (GET/PUT/DELETE).
- **Failles corrigées (OWASP A01) :** un élève pouvait lire les inscriptions / la fiche de n'importe quel élève en changeant l'id dans l'URL.
- **Erreurs :** 400 (validation détaillée par champ, JSON invalide, paramètre invalide, règles de CursusCourseService), 404, 405, 409 (doublon, donnée utilisée, contrainte SQL), 429, 503 ; message générique pour les 500.
- **Données de démo** enrichies : cours ordonnés dans le cursus CDA, 2 promotions, inscription de l'élève de démo.

## #04 – Journal d'audit MongoDB (NoSQL)

- **Date :** 15/10/2026 — **Compétence :** composants d'accès aux données NoSQL
- Paquet `audit` : document `AuditLog`, `AuditLogRepository` (Spring Data MongoDB), `AuditService` (écriture `@Async` tolérante aux pannes, recherche paginée avec `MongoTemplate`), `AuditInterceptor` (toutes les écritures de l'API), `AuditIndexInitializer` (index TTL 365 j + index username), `AuditController` (ADMIN).
- Connexions réussies / échouées / bloquées journalisées depuis `AuthController`.
- **Décision :** l'application doit fonctionner sans MongoDB (dev) → timeout court (2 s), écriture asynchrone, 503 explicite sur la page d'audit.

## #05 – Durcissement sécurité et configuration

- jjwt 0.9.1 + jaxb-api → **jjwt 0.12.6** ; `JwtFilter` relit le compte en base à chaque requête ; bean `UserDetailsService` (plus de mot de passe généré dans les logs).
- **Anti force brute** : 5 échecs → 429 pendant 15 min.
- **Profils Spring :** dev (H2) / `prod` (PostgreSQL, secrets obligatoires, Swagger et H2 désactivés).
- Console H2 réparée (Spring Boot 4 : module `spring-boot-h2console`).
- `docs/SECURITE.md` : OWASP Top 10, ANSSI, RGPD, limites connues.

## #06 – Tests backend et plan de tests

- 9 nouvelles classes de tests (≈ 50 tests) : services, `PlanningCalculator`, `JwtUtils`, `LoginAttemptService`, `AuditService`, **tests d'intégration de bout en bout** (`SecurityIntegrationTest`, 12 scénarios), `AuditMongoIntegrationTest` (MongoDB réel, CI).
- Exécutés sur H2 **et sur PostgreSQL 16** (profil prod) : tout est vert (voir `docs/PLAN_DE_TESTS.md` §5).
- **Bug trouvé par les tests :** Jackson 3 (Spring Boot 4) refuse un `boolean` absent du JSON → `forced` rendu facultatif.

## #07 – Frontend : nouvelles pages et pages branchées sur l'API

- Pages créées : **Promotions**, **Inscriptions**, **Utilisateurs**, **Journal d'audit** (remplacent les pages « bientôt disponible »).
- Pages réécrites : **Cours** (CRUD réel, lecture seule pour le formateur), **Mon planning** (planning calculé par l'API), **Tableau de bord** (chiffres clés / prochain cours).
- Page Cursus (Cyprien) : ajout d'un panneau « Cours du cursus » (ajout / retrait ordonné).
- Données fictives supprimées (`courses.mock.ts`, `calendrier.mock.ts`, composant `course-card`, page `placeholder`).
- Styles communs (`styles.scss`), accessibilité (libellés, `role="alert"`, focus visible), utilitaire `apiErrorMessage`.

## #08 – Docker, déploiement, CI/CD

- `backend/Dockerfile`, `frontend/Dockerfile` (multi-étapes, non-root, healthchecks), `frontend/nginx.conf` (proxy `/api`, en-têtes de sécurité, CSP).
- `docker-compose.yml` (PostgreSQL, MongoDB, backend, frontend) + `.env.example` ; endpoint de santé public `/api/health` (vérifie PostgreSQL) pour les healthchecks.
- `scripts/smoke-test.sh` (test de fumée), `.github/workflows/ci.yml` (backend sur PostgreSQL + MongoDB, frontend, Docker + fumée, publication ghcr.io sur `main`).
- `docs/DEPLOIEMENT.md` (procédure, variables, CI/CD, exploitation, sauvegarde, rollback).
- Environnement Angular de production : `apiUrl = '/api'` (avant : `http://localhost:8080/api` en dur).

## #09 – Tests frontend

- `karma.conf.js` (navigateur `ChromeHeadlessCI`), script `npm run test:ci`.
- 8 fichiers de tests, 24 tests : session, intercepteur, guard, messages d'erreur, pages Cours / Promotions / Utilisateurs — **24/24 réussis**.

## #10 – Gestion de projet

- `docs/GESTION_PROJET.md` (méthode, workflow Git, rituels, mise en place de GitHub Projects) et `docs/backlog.csv`.

---
## #01 – Sécurité JWT de bout en bout

- **Auteur :** Benjamin
- **Date :** 15/10/2026
- **Branche :** `feature/securite-jwt`
- **Activité-type CDA visée :** AT1 « Développer une application sécurisée » (+ AT2 pour l'architecture en couches)

### Situation de départ (problèmes constatés)

1. Le login Angular comparait l'email et le mot de passe avec 4 comptes **écrits en dur** dans `auth.service.ts` : le backend n'était jamais appelé.
2. Côté backend, `SecurityConfig` laissait passer toutes les requêtes (`anyRequest().permitAll()`) : l'API était ouverte sans token.
3. Les `@PreAuthorize` des contrôleurs étaient **inactifs** (pas de `@EnableMethodSecurity`).
4. `JwtFilter` était un simple `@Component` : Spring Boot l'enregistrait comme filtre servlet exécuté **après** Spring Security, donc sans effet sur les droits.
5. Mots de passe comparés **en clair** et secret JWT écrit en dur dans le code.
6. Aucun compte en base : impossible de se connecter via l'API.
7. Rôles incompatibles : `FORMATEUR`/`ETUDIANT` côté front, `TEACHER`/`STUDENT` côté back.
8. Un accès refusé par `@PreAuthorize` renvoyait une erreur 500 au lieu de 403.

### Ce qui a été fait

**Backend (Spring Boot)**

| Fichier | Changement |
|---|---|
| `security/SecurityConfig.java` | API stateless, `JwtFilter` branché dans la chaîne de sécurité, matrice des droits, `@EnableMethodSecurity`, bean `PasswordEncoder` (BCrypt), réponse 401 si non authentifié, CORS 4200 |
| `security/JwtUtils.java` | Secret et durée lus dans la configuration (`app.jwt.*`, surchargeables par `JWT_SECRET` / `JWT_EXPIRATION_MS`), contrôle de la longueur du secret (≥ 32 octets pour HS256) |
| `security/JwtFilter.java` | N'est plus un `@Component` (instancié par `SecurityConfig`) |
| `controller/AuthController.java` | Vérification BCrypt (`passwordEncoder.matches`), message d'erreur générique, `GET /api/auth/me` renvoie l'utilisateur connecté, injection par constructeur |
| `dto/LoginResponse.java` | + `username`, `firstName`, `lastName` |
| `entity/UserAccount.java` | + `firstName`, `lastName` ; `passwordHash` contient un hash BCrypt |
| `config/DataInitializer.java` | **Nouveau** : comptes de démonstration (mots de passe hachés) + filières, cursus, cours, si la base est vide. Désactivable : `app.seed.enabled=false` |
| `exception/GlobalExceptionHandler.java` | `AccessDeniedException` → **403** (au lieu de 500) |
| `resources/application.properties` | Propriétés `app.jwt.secret`, `app.jwt.expiration-ms`, `app.seed.enabled` |

**Matrice des droits**

| Ressource | Public | STUDENT / TEACHER | REFERENTE / ADMIN |
|---|---|---|---|
| `POST /api/auth/login`, Swagger | ✅ | ✅ | ✅ |
| `GET` filières, cursus, cours | ❌ 401 | ✅ | ✅ |
| `POST/PUT/DELETE` filières, cursus, cours | ❌ 401 | ❌ 403 | ✅ |
| `/api/student`, `/api/enrollment` | ❌ 401 | selon `@PreAuthorize` du contrôleur | selon `@PreAuthorize` |

**Frontend (Angular)**

| Fichier | Changement |
|---|---|
| `core/auth/auth.service.ts` | `login()` appelle `POST /api/auth/login` (Observable), stocke token + utilisateur dans `localStorage`, vérifie l'expiration du token |
| `core/auth/auth.interceptor.ts` | **Nouveau** : ajoute `Authorization: Bearer <token>` ; sur 401 → déconnexion + retour au login |
| `app.config.ts` | Enregistrement de l'intercepteur |
| `features/auth/pages/login/*` | Login asynchrone, bouton désactivé pendant l'appel, message si serveur injoignable |
| `shared/models/user-role.model.ts` | Rôles alignés sur le back (`TEACHER`, `STUDENT`) + libellés français `ROLE_LABELS` |
| `shared/models/user.model.ts` | + `id`, interface `LoginResponse` |
| `app-routing/app.routes.ts`, `shared/config/nav.config.ts` | Rôles renommés |

### Comptes de démonstration

| Email | Mot de passe | Rôle |
|---|---|---|
| admin@educmanager.fr | admin123 | ADMIN |
| referente@educmanager.fr | referente123 | REFERENTE |
| formateur@educmanager.fr | formateur123 | TEACHER |
| etudiant@educmanager.fr | etudiant123 | STUDENT |

### Vérifications effectuées

- Backend compilé (`mvn test-compile`) et frontend compilé (`ng build`, dev et prod) sans erreur.
- Tests manuels de l'API (curl) :
  - sans token → `401` ; token invalide → `401` ; mauvais mot de passe → `401` + message générique ;
  - référente : lecture `200`, création de filière `201` ;
  - étudiant : lecture `200`, création de filière `403`, `GET /api/student` → `403` ; admin → `200` ;
  - `GET /api/auth/me` → infos de l'utilisateur du token.
- ⚠️ Les tests JUnit (`mvnw test`) n'ont pas pu être lancés pendant le lot : **à lancer sur le poste avant le commit**.

### Comment vérifier soi-même

1. `start-educmanager.bat` (ou backend + `npm start`).
2. Se connecter avec un compte ci-dessus : le menu dépend du rôle.
3. Dans les outils du navigateur (F12 → Réseau), les appels `/api/...` portent l'en-tête `Authorization: Bearer ...`.
4. Se connecter en étudiant puis appeler `POST /api/filieres` depuis Swagger → 403.

### Reste à faire / points connus (au moment du lot #01)

> Mise à jour : ces quatre points ont été traités dans les lots #03, #04 et #05.

- La console H2 (`/h2-console`) ne répond pas : avec Spring Boot 4 il faut ajouter la dépendance `spring-boot-h2console` (lot #05).
- Le message « Using generated security password » au démarrage est sans effet (aucun formulaire de login Spring) ; on pourra le supprimer plus tard.
- `GlobalExceptionHandler.handleException` renvoie le message technique de l'exception au client : à remplacer par un message générique.
- Pas encore de page de gestion des utilisateurs (lot #04) ni de tests automatisés sur la sécurité.

---

## #00 – Mise en place de l'environnement local

- **Auteur :** Benjamin — **Date :** 15/10/2026
- Clone du dépôt dans `IdeaProjects/EducManager`, JDK 21 (`.jdks/ms-21.0.8`), Node.js.
- Script `start-educmanager.bat` (non versionné, listé dans `.git/info/exclude`) : lance le backend (port 8080) et le frontend (port 4200) dans deux fenêtres.
- Problème rencontré : page blanche `ERR_NETWORK_CHANGED` au premier chargement → résolu par un rechargement (Ctrl+F5).
