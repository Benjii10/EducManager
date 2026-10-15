# Sécurité — EducManager (OWASP, ANSSI, RGPD)

> [Benjamin – #05, 2026-10-15] Création. Recense les mesures réellement implémentées et les risques restants.

## 1. OWASP Top 10 (2021) — mesures en place

| Risque OWASP | Mesures dans EducManager | Où (code / config) | Vérifié par |
|---|---|---|---|
| **A01 Contrôle d'accès défaillant** | Toutes les routes protégées par défaut (`anyRequest().authenticated()`) ; matrice de droits par URL + `@PreAuthorize` par méthode ; un élève ne lit que **ses** inscriptions / sa fiche (correction d'une faille IDOR existante) ; le compte est relu en base à chaque requête (compte supprimé = accès coupé) | `SecurityConfig`, `EnrollmentController`, `StudentController`, `JwtFilter` | `SecurityIntegrationTest` (401 / 403) |
| **A02 Défaillances cryptographiques** | Mots de passe hachés **BCrypt** (salage automatique) ; JWT signé HMAC-SHA256 avec un secret ≥ 256 bits fourni par variable d'environnement (aucun secret en production dans le code) ; HTTPS à placer devant nginx en production | `SecurityConfig.passwordEncoder`, `JwtUtils`, `application-prod.properties` | `UserServiceTest`, `JwtUtilsTest` |
| **A03 Injection** | Accès aux données uniquement via JPA / Spring Data (requêtes paramétrées) ; recherche MongoDB avec `Pattern.quote` (pas d'injection d'expression régulière) ; Angular échappe toutes les valeurs affichées (pas de `innerHTML`) ; CSP stricte | Repositories, `AuditService.search`, `nginx.conf` | revue de code |
| **A04 Conception non sécurisée** | Règles métier côté serveur (le front ne fait que de l'ergonomie) ; erreurs métier explicites (400 / 409) | services | tests de services |
| **A05 Mauvaise configuration** | Swagger et console H2 désactivés en production ; seul `/api/health` est public (sans détail technique) ; erreurs 500 sans détail technique ; en-têtes de sécurité nginx (CSP, X-Frame-Options, nosniff, Referrer-Policy) ; CORS limité aux origines configurées ; conteneurs non-root | `application-prod.properties`, `GlobalExceptionHandler`, `nginx.conf`, `Dockerfile` | `scripts/smoke-test.sh` |
| **A06 Composants vulnérables / obsolètes** | jjwt 0.9.1 (2018, abandonné) remplacé par jjwt 0.12 ; dépendances gérées par la BOM Spring Boot | `pom.xml` | build CI |
| **A07 Identification et authentification** | Message d'erreur générique ; **blocage après 5 échecs pendant 15 min (HTTP 429)** ; politique de mot de passe (8 caractères, lettre + chiffre) ; token à durée limitée (24 h) avec contrôle d'expiration côté front | `LoginAttemptService`, `CreateUserRequest`, `auth.service.ts` | `LoginAttemptServiceTest`, `SecurityIntegrationTest.bloqueApresCinqEchecs` |
| **A08 Intégrité des logiciels et données** | Images construites et testées par la CI avant publication ; validation `@Valid` de toutes les entrées | `.github/workflows/ci.yml`, DTO | CI |
| **A09 Journalisation et surveillance** | **Journal d'audit MongoDB** : connexions réussies / échouées / bloquées et toutes les écritures (utilisateur, ressource, code HTTP, IP) ; consultable par l'admin | `audit/*` | `AuditMongoIntegrationTest`, test de fumée |
| **A10 SSRF** | L'application n'appelle aucune URL fournie par l'utilisateur | — | — |

## 2. Recommandations ANSSI appliquées

- Mots de passe : stockage par fonction de hachage lente et salée (BCrypt), longueur minimale, limitation des tentatives.
- Principe du moindre privilège : 4 rôles, chaque page / route limitée au strict nécessaire ; le backend n'est pas exposé hors du réseau Docker ; conteneurs exécutés sans droits root.
- Secrets hors du code source : `.env` (non versionné, modèle `.env.example`), variables d'environnement.
- Journalisation des événements de sécurité avec horodatage et durée de conservation définie.
- Défense en profondeur : contrôles côté front **et** côté back, contraintes d'intégrité en base.

## 3. RGPD

| Principe | Application |
|---|---|
| Minimisation | Données personnelles limitées au nom, prénom et email (nécessaires à l'inscription et à la connexion) ; aucune donnée sensible |
| Finalité | Gestion pédagogique et sécurité de l'application |
| Accès limité | Un élève ne voit que ses propres données ; la liste des élèves est réservée à la référente et à l'admin |
| Durée de conservation | Journal d'audit (contient des adresses IP) supprimé automatiquement après 365 jours (index TTL MongoDB, paramétrable) |
| Droit à l'effacement | L'admin peut supprimer un compte (après suppression des inscriptions) |
| Sécurité | Voir §1 |

**À faire avant une vraie mise en production :** mention d'information RGPD sur la page de connexion, registre des traitements, procédure d'export des données d'un élève (droit d'accès), HTTPS obligatoire.

## 4. Risques connus et limites

| Limite | Impact | Piste |
|---|---|---|
| Token JWT stocké dans `localStorage` | Lisible en cas de faille XSS (atténué par la CSP et l'échappement Angular) | Cookie `HttpOnly` + `SameSite` |
| Pas de révocation de token avant expiration (hors suppression du compte) | Un token volé reste valide 24 h | Durée plus courte + refresh token |
| Compteur anti force brute en mémoire | Remis à zéro au redémarrage, non partagé entre plusieurs instances | Redis |
| HTTPS non configuré dans le `docker-compose` | Trafic en clair si exposé tel quel | Reverse proxy TLS (Traefik / Caddy / certificat Let's Encrypt) |
