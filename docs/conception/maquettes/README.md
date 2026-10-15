# Maquettes (wireframes)

> [Benjamin – #02, 2026-10-15] Maquettes « fil de fer » des écrans ajoutés en phase 2, réalisées avant le développement
> à partir des user stories du [cahier des charges](../CAHIER_DES_CHARGES.md). Format SVG (s'ouvre dans un navigateur).
> Elles peuvent être reprises dans Figma pour une version graphique.

| Fichier | Écran | User story |
|---|---|---|
| [01-connexion.svg](01-connexion.svg) | Connexion, message d'erreur générique, blocage | US01 |
| [02-promotions.svg](02-promotions.svg) | Liste et formulaire des promotions | US05 |
| [03-inscriptions.svg](03-inscriptions.svg) | Inscription d'un élève, liste filtrable | US06 |
| [04-utilisateurs.svg](04-utilisateurs.svg) | Gestion des comptes (admin) | US08 |
| [05-planning-etudiant.svg](05-planning-etudiant.svg) | Planning calculé de l'élève | US07 |
| [06-journal-audit.svg](06-journal-audit.svg) | Journal d'audit filtrable et paginé | US09 |

**Principes d'ergonomie appliqués :** même gabarit pour toutes les pages (menu latéral selon le rôle, titre,
formulaire en haut, tableau en dessous), messages de confirmation en vert et d'erreur en rouge à côté de l'action,
libellés visibles au-dessus de chaque champ, champs obligatoires marqués `*`, actions destructives confirmées.

**Enchaînement des écrans (navigation) :** Connexion → page par défaut du rôle (ADMIN : Utilisateurs ; REFERENTE : Filières ;
TEACHER : Cours ; STUDENT : Mon planning) → menu latéral limité aux pages autorisées pour le rôle.
