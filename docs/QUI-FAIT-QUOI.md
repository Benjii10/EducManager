# Qui fait quoi — EducManager

Ce fichier sert à savoir **qui a réalisé quelle partie** du projet (utile pour l'équipe et pour les dossiers professionnels CDA).
Il complète l'historique Git (`git log --author=...`).

## 1. Convention de traçabilité dans le code

| Où | Format | Exemple |
|---|---|---|
| Nouvelle classe Java | Javadoc avec `@author` + n° de lot | `@author Benjamin` |
| Classe ou ligne modifiée | Commentaire `[Prénom – #NN, AAAA-MM-JJ]` | `// [Benjamin – #01, 2026-10-15] ...` |
| Fichier TypeScript | Même commentaire `[Prénom – #NN]` | `// [Benjamin – #01] ...` |
| Détail d'un lot | Entrée `#NN` dans `docs/JOURNAL_DEV.md` | `#01 – Sécurité JWT de bout en bout` |
| Git | 1 branche par lot, commits préfixés `feat:` / `fix:` / `docs:` / `test:` | `feature/securite-jwt` |

Pour retrouver tout ce qu'une personne a fait : rechercher `[Benjamin` (Ctrl+Shift+F dans IntelliJ / VS Code).

## 2. Répartition — phase 1 (juin 2026, d'après l'historique Git)

| Personne (compte Git) | Périmètre principal |
|---|---|
| **Cyprien** (`Cyp`) | Structure initiale du projet, architecture backend ; Filière et Cursus de bout en bout (entités, repositories, DTO, mappers, services avec validations métier, contrôleurs, gestion des exceptions, tests) ; pages Angular Filières et Cursus |
| **Manar** (`Manar`, `Manar Lamrani`) | Entité et API Cours (+ association Cursus–Cours, ordre, contrôle d'accès), leurs tests ; layout Angular (sidebar, routing, guards, rôles), design du front, pages Cours et Calendrier (données fictives) |
| **Benjamin** (`Benjii10`) | Comptes utilisateurs et rôles (`UserAccount`, `Role`), étudiants et inscriptions (`Student`, `Enrollment`, `EnrollmentType`), leurs repositories, DTO et contrôleurs ; première version de la sécurité JWT (`JwtUtils`, `JwtFilter`, `AuthController`) et CORS |

> À relire et corriger ensemble si une ligne ne correspond pas à la réalité.

## 3. Répartition — phase 2 (octobre 2026)

Tous les lots de la phase 2 ont été réalisés par **Benjamin avec l'assistance de Claude (IA)**.

| Lot | Sujet | Responsable | Statut |
|---|---|---|---|
| #00 | Mise en place de l'environnement local (script de lancement) | Benjamin | ✅ |
| #01 | Sécurité JWT de bout en bout (BCrypt, routes protégées, vrai login Angular, intercepteur) | Benjamin | ✅ |
| #02 | Conception : cahier des charges, user stories, maquettes, MCD/MLD, architecture | Benjamin | ✅ |
| #03 | Métier : promotions, inscriptions, comptes, cours CRUD, planning | Benjamin | ✅ |
| #04 | Journal d'audit MongoDB | Benjamin | ✅ |
| #05 | Durcissement sécurité, profils dev/prod, PostgreSQL, console H2 | Benjamin | ✅ |
| #06 | Tests backend (unitaires + intégration H2 / PostgreSQL) et plan de tests | Benjamin | ✅ |
| #07 | Pages Angular (cours, promotions, inscriptions, utilisateurs, audit, planning, tableau de bord) | Benjamin | ✅ |
| #08 | Docker, docker compose, nginx, CI/CD GitHub Actions, déploiement | Benjamin | ✅ |
| #09 | Tests frontend | Benjamin | ✅ |
| #10 | Gestion de projet (backlog, workflow) | Benjamin | ✅ |

### Fichiers de l'équipe modifiés en phase 2 (et pourquoi)

| Fichier (auteur d'origine) | Modification |
|---|---|
| `CourseService`, `CourseController`, `Course` (Manar) | Ajout lecture par id / modification / suppression, validation, code unique — contrat existant conservé |
| `CursusCourseRepository` (Manar) | + `existsByCourse_Id` |
| `GlobalExceptionHandler` (Cyprien) | Nouveaux cas d'erreur (400, 403, 409, 429, 503), message générique pour les 500 |
| `cursus-list` Angular (Cyprien) | Panneau « Cours du cursus » |
| `cours-list`, `calendrier` Angular (Manar) | Branchés sur l'API à la place des données fictives |
| `app.routes.ts`, `nav.config.ts`, `auth.service.ts` (Manar) | Rôles alignés sur le back, nouvelles pages, vrai login |

Les tests écrits par l'équipe en phase 1 n'ont **pas** été modifiés et passent tous.
