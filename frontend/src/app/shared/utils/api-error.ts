import { HttpErrorResponse } from '@angular/common/http';

// [Benjamin – #07, 2026-10-15] Création.
/**
 * Transforme une erreur HTTP de l'API en message lisible pour l'utilisateur.
 * L'API renvoie { message, fields? } (voir GlobalExceptionHandler côté back).
 */
export function apiErrorMessage(error: unknown, fallback = 'Une erreur est survenue.'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }
  if (error.status === 0) {
    return 'Serveur injoignable. Vérifiez votre connexion.';
  }
  if (error.status === 403) {
    return "Vous n'avez pas les droits pour cette action.";
  }
  const body = error.error as { message?: string; fields?: Record<string, string> } | null;
  if (body?.fields && Object.keys(body.fields).length > 0) {
    return Object.values(body.fields).join(' — ');
  }
  return body?.message ?? fallback;
}
