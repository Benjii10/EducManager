// [Benjamin – #08, 2026-10-15] En production, le frontend est servi par nginx qui relaie /api vers le
// backend (même origine : pas de CORS, pas d'URL en dur). Avant : http://localhost:8080/api.
export const environment = {
  production: true,
  apiUrl: '/api',
};
