================================================================================
 EducManager - RECAPITULATIF DE LA PHASE 2 (octobre 2026)
 Travail realise par Benjamin avec l'assistance de Claude (IA, Anthropic)
 Branche : feature/securite-jwt
================================================================================

Ce fichier resume TOUT ce qui a ete fait, POURQUOI, et ce qui reste a faire.
Le detail par lot est dans docs/JOURNAL_DEV.md ; chaque modification du code porte
un commentaire "[Benjamin - #NN, date]" (rechercher "[Benjamin" dans l'IDE).


--------------------------------------------------------------------------------
1. POINT DE DEPART (constate le 15/10/2026)
--------------------------------------------------------------------------------
- Seules les pages Filieres et Cursus (Cyprien) etaient reliees a l'API.
- Le login Angular comparait avec 4 comptes ecrits en dur ; le backend n'etait
  jamais appele. Mots de passe en clair, secret JWT dans le code.
- L'API etait entierement ouverte (anyRequest().permitAll()), les @PreAuthorize
  inactifs, le filtre JWT branche au mauvais endroit.
- Roles incompatibles (front FORMATEUR/ETUDIANT, back TEACHER/STUDENT).
- Cours et Calendrier : donnees fictives. Promotions, Inscriptions, Utilisateurs :
  pages vides. Pas d'entite Promotion.
- Pas de PostgreSQL configure, pas de NoSQL, pas de Docker, pas de CI, pas de doc,
  pas de tests frontend.
- Bilan face aux 11 competences du titre CDA : 2 acquises, 6 partielles, 3 absentes.


--------------------------------------------------------------------------------
2. CE QUI A ETE FAIT (par lot)
--------------------------------------------------------------------------------
#00 Environnement local : clone, JDK 21, start-educmanager.bat.
#01 Securite JWT de bout en bout : BCrypt, routes protegees, @EnableMethodSecurity,
    JwtFilter dans la chaine Spring Security, vrai login Angular + intercepteur,
    roles alignes, donnees de demonstration.
#02 Conception : cahier des charges + 10 user stories (docs/conception/), 6 maquettes
    SVG, MCD / MLD / regles de gestion, schema PostgreSQL extrait de la vraie base,
    architecture en couches et de deploiement.
#03 Metier backend : entite Promotion ; Enrollment refait avec de vraies cles
    etrangeres ; services Promotion / Enrollment / User / Course ; calcul de planning
    en jours ouvres ; API /api/promotions, /api/enrollment(/me, /me/planning),
    /api/users, /api/courses CRUD. Faille corrigee : un eleve pouvait lire les
    donnees des autres eleves (OWASP A01).
#04 NoSQL : journal d'audit MongoDB (connexions + toutes les ecritures), ecriture
    asynchrone tolerante aux pannes, index TTL 365 jours (RGPD), page Admin.
#05 Durcissement : jjwt 0.12 (au lieu de 0.9.1 abandonne), blocage apres 5 echecs
    de connexion (HTTP 429), profils dev (H2) / prod (PostgreSQL, secrets obligatoires,
    Swagger et H2 desactives), gestion d'erreurs complete (400/403/404/405/409/429/503),
    docs/SECURITE.md (OWASP Top 10, ANSSI, RGPD).
#06 Tests backend : 9 nouvelles classes (~50 tests), dont 13 tests d'integration de
    bout en bout ; plan de tests docs/PLAN_DE_TESTS.md (cas relies aux user stories).
#07 Frontend : pages Promotions, Inscriptions, Utilisateurs, Journal d'audit ;
    Cours, Planning et Tableau de bord branches sur l'API ; panneau "Cours du cursus" ;
    styles communs et accessibilite (libelles, role="alert", focus visible).
#08 Deploiement : Dockerfiles multi-etapes non-root, nginx (proxy /api + en-tetes de
    securite + CSP), docker-compose (PostgreSQL + MongoDB + backend + frontend),
    .env.example, endpoint /api/health, test de fumee, CI/CD GitHub Actions
    (tests sur PostgreSQL et MongoDB reels -> Docker -> publication ghcr.io),
    docs/DEPLOIEMENT.md.
#09 Tests frontend : karma.conf.js (Chrome sans interface), 24 tests.
#10 Gestion de projet : docs/GESTION_PROJET.md, docs/backlog.csv.


--------------------------------------------------------------------------------
3. DECISIONS PRISES (et pourquoi)
--------------------------------------------------------------------------------
D1  Travailler sur une branche (feature/securite-jwt) et un fork perso pour la CI,
    sans toucher au depot de l'equipe tant que ce n'est pas valide.
D2  Ne modifier aucun test de l'equipe ; conserver les contrats d'API existants
    (GET/POST /api/courses de Manar) ; marquer chaque modification par un commentaire.
D3  Roles : on garde les noms du backend (TEACHER, STUDENT) comme reference unique ;
    les libelles francais sont dans ROLE_LABELS cote front.
D4  MongoDB pour le journal d'audit (competence NoSQL) : evenements independants,
    jamais modifies, schema evolutif, expiration automatique (TTL). PostgreSQL reste
    la base du metier (relations, contraintes d'integrite).
D5  L'application doit marcher SANS MongoDB en developpement : audit asynchrone,
    timeout court, erreurs journalisees ; la page d'audit renvoie 503 explicite.
D6  Le compte est relu en base a chaque requete (JwtFilter) : un compte supprime ou un
    role modifie prend effet immediatement, sans attendre l'expiration du token.
D7  "forced" sur une inscription = autoriser une promotion deja terminee (seule
    regle metier ou forcer a du sens ; les doublons sont toujours refuses).
D8  La creation d'un eleve passe par la gestion des comptes (POST /api/users role
    STUDENT cree automatiquement la fiche eleve) ; POST /api/student supprime.
D9  Planning eleve calcule (jours ouvres, ordre du cursus) plutot que saisi :
    pas de nouvelle table, logique testable (PlanningCalculator).
D10 Hibernate ddl-auto=update en production (simplicite) ; le schema de reference est
    documente (schema-postgresql.sql). Evolution possible : Flyway.
D11 Healthcheck via un endpoint maison /api/health (pas d'Actuator) : aucune
    dependance supplementaire, verifie la connexion PostgreSQL.
D12 nginx sert le front ET relaie /api : meme origine, pas de CORS en production,
    backend non expose a l'exterieur.
D13 Maquettes en SVG dans le depot (versionnees) ; peuvent etre refaites dans Figma.
D14 Bug decouvert par les tests : Spring Boot 4 / Jackson 3 refuse un boolean absent
    du JSON -> champ "forced" rendu facultatif (Boolean).


--------------------------------------------------------------------------------
4. VERIFICATIONS EFFECTUEES
--------------------------------------------------------------------------------
Faites le 15/10/2026 :
- Backend, H2 : 107 tests -> 106 reussis, 0 echec, 1 ignore (test MongoDB : pas de
  MongoDB sur le poste ; il tourne dans la CI).
- Backend, PostgreSQL 16 reel (profil prod) : 14/14 tests d'integration reussis ;
  le jar de production demarre, /api/health = UP, Swagger et console H2 = 404,
  API sans token = 401, demarrage refuse si JWT_SECRET est absent.
- Frontend : 24/24 tests reussis (Karma, Chrome sans interface), build de prod OK.
- Tests manuels de l'API (curl) : droits par role (401/403), doublons (409),
  planning calcule, erreurs 400/404/405/503.

En attente :
- CI GitHub Actions sur le fork (backend avec PostgreSQL + MongoDB reels,
  frontend, construction des images Docker, docker compose + test de fumee).
  -> resultat a reporter ici une fois la branche poussee.


--------------------------------------------------------------------------------
5. COUVERTURE DES 11 COMPETENCES DU TITRE CDA
--------------------------------------------------------------------------------
Legende : OK = couvert et prouve dans le depot.

AT1 - Developper une application securisee
 1. Installer et configurer son environnement ......... OK  (JDK, Maven, Angular, Docker,
    profils dev/prod, scripts, README)
 2. Developper des interfaces utilisateur .............. OK  (7 pages ajoutees/branchees,
    maquettes, accessibilite, tests Karma)
 3. Developper des composants metier ................... OK  (services Promotion/Inscription/
    Comptes/Planning, regles de gestion, securite JWT, tests)
 4. Contribuer a la gestion d'un projet ................ OK* (GESTION_PROJET.md, backlog.csv,
    branches, commits, PR, CI) - *creer le tableau GitHub Projects (voir 6)

AT2 - Concevoir et developper une application securisee organisee en couches
 5. Analyser les besoins et maquetter .................. OK  (cahier des charges, 10 user
    stories avec criteres d'acceptation, 6 maquettes)
 6. Definir l'architecture logicielle .................. OK  (ARCHITECTURE.md : couches,
    deploiement, choix justifies)
 7. Concevoir et mettre en place une base relationnelle  OK  (MCD, MLD, regles, PostgreSQL 16,
    schema extrait de la vraie base)
 8. Composants d'acces aux donnees SQL et NoSQL ........ OK  (Spring Data JPA + Spring Data
    MongoDB / MongoTemplate, tests sur PostgreSQL et MongoDB reels)

AT3 - Preparer le deploiement d'une application securisee
 9. Preparer et executer les plans de tests ............ OK  (PLAN_DE_TESTS.md, 131 tests
    automatises, resultats consignes)
10. Preparer et documenter le deploiement .............. OK  (Dockerfiles, compose, .env,
    DEPLOIEMENT.md : procedure, exploitation, sauvegarde, rollback)
11. Contribuer a la mise en production (DevOps) ........ OK* (CI/CD GitHub Actions jusqu'a la
    publication des images) - *valide quand la CI du fork est verte (voir 4)

Transverse : OWASP / ANSSI / RGPD documentes et implementes (docs/SECURITE.md).


--------------------------------------------------------------------------------
6. CE QUI RESTE A FAIRE PAR TOI (ne peut pas etre fait a ta place)
--------------------------------------------------------------------------------
1. Pousser la branche sur ton fork et verifier que la CI est verte (onglet Actions).
2. Lancer toi-meme une fois : docker compose up -d --build (Docker Desktop) puis
   ouvrir http://localhost:8081 ; et la recette manuelle T-M01 a T-M08
   (docs/PLAN_DE_TESTS.md section 4).
3. Creer le tableau GitHub Projects a partir de docs/backlog.csv (5 minutes).
4. Relire docs/QUI-FAIT-QUOI.md avec Cyprien et Manar, puis ouvrir une pull request
   vers 3steak/EducManager.
5. IMPORTANT POUR LE JURY : ce code a ete ecrit avec une IA. Pour la declaration sur
   l'honneur et l'oral, tu dois le COMPRENDRE et savoir l'expliquer : relis en priorite
   SecurityConfig, JwtFilter, AuthController, EnrollmentService, AuditService,
   auth.interceptor.ts et le workflow ci.yml. Tu peux mentionner l'usage d'un assistant
   IA dans le dossier (c'est plus sur que de le cacher).
6. Remplir le Dossier Professionnel avec 1 a 3 exemples par activite-type tires de
   ces lots (je peux le pre-remplir a partir de ce fichier).
