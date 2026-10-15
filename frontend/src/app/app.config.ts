import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app-routing/app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // [Benjamin – #01] l'intercepteur ajoute le token JWT à chaque appel API
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
