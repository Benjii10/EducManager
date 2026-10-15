# Cahier des charges — EducManager

> [Benjamin – #02, 2026-10-15] Rédigé a posteriori à partir de l'existant (phase 1 de l'équipe) et complété
> pour les fonctionnalités ajoutées en phase 2. À relire avec l'équipe.

## 1. Contexte

Un organisme de formation gère aujourd'hui ses filières, ses cursus, ses cours, ses promotions et les inscriptions
de ses élèves avec des tableurs partagés. Les problèmes sont : doublons d'inscription, aucune traçabilité de qui
modifie quoi, pas de vue « planning » pour les élèves, données personnelles accessibles à tous.

**Objectif :** une application web sécurisée qui centralise le référentiel pédagogique et les inscriptions,
avec des droits différents selon le profil de l'utilisateur.

## 2. Acteurs (rôles)

| Rôle (code) | Qui | Besoins principaux |
|---|---|---|
| `ADMIN` | Administrateur de l'application | Gérer les comptes, consulter le journal d'audit, accès complet |
| `REFERENTE` | Référente pédagogique | Gérer filières, cursus, cours, promotions et inscriptions |
| `TEACHER` | Formateur | Consulter les cours |
| `STUDENT` | Élève | Consulter son planning et ses inscriptions, uniquement les siennes |

## 3. Périmètre fonctionnel

| Module | Fonctions | Rôles |
|---|---|---|
| Authentification | Connexion par email + mot de passe, déconnexion, session limitée dans le temps | Tous |
| Filières | Lister, créer, modifier, supprimer | REFERENTE, ADMIN |
| Cursus | CRUD, rattachement à une filière, liste ordonnée des cours du cursus | REFERENTE, ADMIN |
| Cours | CRUD (code unique, durée en jours) ; lecture seule pour le formateur | REFERENTE, ADMIN (TEACHER en lecture) |
| Promotions | CRUD (session datée d'un cursus) | REFERENTE, ADMIN |
| Inscriptions | Inscrire un élève à une promotion ou à un cours à l'unité, désinscrire | REFERENTE, ADMIN |
| Planning | Calcul des dates des cours de l'élève à partir de ses promotions | STUDENT |
| Comptes | Créer / modifier / supprimer les comptes, attribuer un rôle | ADMIN |
| Journal d'audit | Historique des connexions et des modifications, filtrable | ADMIN |

**Hors périmètre** (évolutions possibles) : notes et évaluations, absences, salles, envoi d'emails, import CSV.

## 4. User stories et critères d'acceptation

| ID | En tant que… | je veux… | afin de… | Critères d'acceptation (tests) |
|---|---|---|---|---|
| US01 | utilisateur | me connecter avec mon email et mon mot de passe | accéder aux fonctions de mon rôle | Identifiants corrects → page par défaut du rôle ; incorrects → message générique « Email ou mot de passe incorrect » ; 5 échecs → blocage 15 min (T-B01 à T-B03, T-F01) |
| US02 | utilisateur | être déconnecté quand ma session expire | protéger mon compte | Token expiré → retour à la page de connexion (T-F02, T-F03) |
| US03 | référente | gérer les cours (code, titre, durée) | tenir le catalogue à jour | Code unique (409 sinon), durée 1–200 j, suppression interdite si le cours est utilisé (T-B10, T-B11) |
| US04 | référente | ordonner les cours d'un cursus | définir le parcours pédagogique | Ordre unique dans le cursus, un cours une seule fois par cursus (tests CursusCourse existants) |
| US05 | référente | créer une promotion datée pour un cursus | ouvrir une session de formation | Nom unique, date de fin après date de début, suppression interdite s'il y a des inscrits (T-B12 à T-B14, T-F05) |
| US06 | référente | inscrire un élève à une promotion ou à un cours | gérer les effectifs | Pas de doublon (409) ; promotion terminée refusée sauf « forcer » (T-B15 à T-B17) |
| US07 | élève | voir mon planning | savoir quand ont lieu mes cours | Cours de mes promotions datés en jours ouvrés, dans l'ordre du cursus ; je ne vois que mes données (T-B18, T-B08) |
| US08 | administrateur | créer des comptes et attribuer un rôle | donner accès à l'application | Email unique, mot de passe ≥ 8 caractères avec lettre et chiffre, haché ; un compte élève crée sa fiche élève (T-B19 à T-B21, T-F06) |
| US09 | administrateur | consulter le journal des connexions et modifications | détecter un usage anormal | Chaque connexion (réussie / échouée / bloquée) et chaque écriture est enregistrée ; filtre par utilisateur et action ; conservation limitée à 365 jours (T-B22, T-B23) |
| US10 | formateur | consulter les cours | préparer mes interventions | Liste des cours visible, aucun bouton de modification ; l'API refuse l'écriture (403) (T-B07, T-F04) |

## 5. Exigences non fonctionnelles

| Domaine | Exigence |
|---|---|
| Sécurité | Authentification JWT, mots de passe hachés (BCrypt), contrôle des droits côté serveur, protection force brute, OWASP Top 10 (voir `docs/SECURITE.md`) |
| RGPD | Données personnelles minimales (nom, prénom, email), accès limité par rôle, journal d'audit conservé 365 jours maximum |
| Accessibilité | Formulaires avec libellés associés, messages d'erreur annoncés (`role="alert"`), focus visible, contraste suffisant |
| Performance | Réponse API < 500 ms en usage normal ; l'audit est écrit en tâche de fond |
| Disponibilité | L'application fonctionne même si MongoDB (audit) est indisponible |
| Déploiement | Conteneurs Docker, configuration par variables d'environnement, CI/CD GitHub Actions |
| Compatibilité | Navigateurs récents (Chrome, Firefox, Edge) |

## 6. Contraintes techniques

- Backend : Java 21, Spring Boot 4, Spring Security, Spring Data JPA (PostgreSQL) et Spring Data MongoDB.
- Frontend : Angular 19 (composants standalone).
- Bases : PostgreSQL 16 (données métier, relationnelles) ; MongoDB 7 (journal d'audit) ; H2 en mémoire pour le développement.
- Code source : GitHub, une branche par fonctionnalité, revue par pull request.
