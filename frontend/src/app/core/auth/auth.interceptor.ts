import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

/**
 * [Benjamin – #01, 2026-10-15] Création.
 *
 * 1. Ajoute l'en-tête "Authorization: Bearer <token>" à chaque appel vers notre API.
 * 2. Si l'API répond 401 (token absent, expiré ou invalide) en dehors du login,
 *    on déconnecte l'utilisateur et on le renvoie vers /login.
 * Enregistré dans app.config.ts via provideHttpClient(withInterceptors([authInterceptor])).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const isApiCall = req.url.startsWith(environment.apiUrl);
  const isLoginCall = req.url.endsWith('/auth/login');
  const token = auth.getToken();

  const request =
    isApiCall && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && isApiCall && !isLoginCall) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
