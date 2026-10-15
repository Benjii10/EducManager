# EducManager

Application web de gestion pédagogique : filières, cursus, cours, promotions, inscriptions des élèves,
planning des élèves, gestion des comptes et journal d'audit.

**Stack :** Spring Boot 4 (Java 21) · Spring Security JWT · Angular 19 · PostgreSQL 16 · MongoDB 7 · Docker · GitHub Actions

## Démarrage rapide

### Avec Docker (recommandé, identique à la production)

```bash
cp .env.example .env        # puis remplacer les mots de passe et le JWT_SECRET
docker compose up -d --build
```
→ http://localhost:8081

### En développement (H2 en mémoire, sans Docker)

Prérequis : JDK 21, Node.js 20+.

```bash
cd backend && ./mvnw spring-boot:run      # http://localhost:8080 (Swagger : /swagger-ui.html, H2 : /h2-console)
cd frontend && npm install && npm start   # http://localhost:4200
```
Sous Windows : `start-educmanager.bat` (lance les deux). MongoDB est facultatif en développement (le journal d'audit est alors désactivé de fait).

### Comptes de démonstration

| Email | Mot de passe | Rôle |
|---|---|---|
| admin@educmanager.fr | admin123 | ADMIN |
| referente@educmanager.fr | referente123 | REFERENTE |
| formateur@educmanager.fr | formateur123 | TEACHER |
| etudiant@educmanager.fr | etudiant123 | STUDENT |

## Tests

```bash
cd backend && ./mvnw test           # JUnit (unitaires + intégration sur H2)
cd frontend && npm run test:ci      # Jasmine/Karma (Chrome sans interface)
bash scripts/smoke-test.sh          # après docker compose up
```
La CI (`.github/workflows/ci.yml`) lance tout, avec PostgreSQL et MongoDB réels, à chaque push.

## Documentation

| Document | Contenu |
|---|---|
| [`readme.txt`](readme.txt) | Récapitulatif de tout ce qui a été fait en phase 2 et des décisions prises |
| [`docs/conception/CAHIER_DES_CHARGES.md`](docs/conception/CAHIER_DES_CHARGES.md) | Besoins, rôles, user stories, exigences |
| [`docs/conception/maquettes/`](docs/conception/maquettes/) | Wireframes des écrans |
| [`docs/conception/MODELE_DE_DONNEES.md`](docs/conception/MODELE_DE_DONNEES.md) | MCD, MLD, règles de gestion, modèle MongoDB |
| [`docs/conception/ARCHITECTURE.md`](docs/conception/ARCHITECTURE.md) | Architecture en couches, déploiement, choix techniques |
| [`docs/SECURITE.md`](docs/SECURITE.md) | OWASP Top 10, ANSSI, RGPD |
| [`docs/PLAN_DE_TESTS.md`](docs/PLAN_DE_TESTS.md) | Stratégie, cas de tests, résultats |
| [`docs/DEPLOIEMENT.md`](docs/DEPLOIEMENT.md) | Docker, variables, CI/CD, exploitation |
| [`docs/GESTION_PROJET.md`](docs/GESTION_PROJET.md) | Méthode, backlog, workflow Git |
| [`docs/QUI-FAIT-QUOI.md`](docs/QUI-FAIT-QUOI.md) | Répartition du travail, convention de traçabilité |
| [`docs/JOURNAL_DEV.md`](docs/JOURNAL_DEV.md) | Journal détaillé des lots de développement |
