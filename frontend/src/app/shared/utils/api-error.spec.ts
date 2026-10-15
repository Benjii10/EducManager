import { HttpErrorResponse } from '@angular/common/http';

import { apiErrorMessage } from './api-error';

// [Benjamin – #09, 2026-10-15] Création.
describe('apiErrorMessage', () => {
  it('renvoie le message de l\'API', () => {
    const err = new HttpErrorResponse({ status: 409, error: { message: 'Doublon' } });
    expect(apiErrorMessage(err)).toBe('Doublon');
  });

  it('assemble les erreurs de validation par champ', () => {
    const err = new HttpErrorResponse({ status: 400, error: { message: 'Données invalides', fields: { email: 'Email invalide', password: 'Trop court' } } });
    expect(apiErrorMessage(err)).toBe('Email invalide — Trop court');
  });

  it('gère le serveur injoignable et le 403', () => {
    expect(apiErrorMessage(new HttpErrorResponse({ status: 0 }))).toContain('injoignable');
    expect(apiErrorMessage(new HttpErrorResponse({ status: 403 }))).toContain('droits');
  });

  it('utilise le message par défaut sinon', () => {
    expect(apiErrorMessage(new Error('x'), 'Défaut')).toBe('Défaut');
  });
});
