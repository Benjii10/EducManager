// [Benjamin – #01, 2026-10-15] rôles renommés : FORMATEUR → TEACHER, ETUDIANT → STUDENT (alignés sur le back)
// [Benjamin – #07, 2026-10-15] pages Promotions / Inscriptions / Utilisateurs réelles (fin des pages
// « bientôt disponible »), page Journal d'audit, accès ADMIN aux pages du référentiel.
import { Routes } from '@angular/router';

import { authGuard } from '../core/auth/auth.guard';
import { defaultRouteGuard } from '../core/auth/default-route.guard';
import { guestGuard } from '../core/auth/guest.guard';
import { roleGuard } from '../core/auth/role.guard';
import { LoginComponent } from '../features/auth/pages/login/login.component';
import { AuditListComponent } from '../features/audit/pages/audit-list/audit-list.component';
import { CalendrierComponent } from '../features/calendrier/pages/calendrier/calendrier.component';
import { CoursListComponent } from '../features/cours/pages/cours-list/cours-list.component';
import { DashboardComponent } from '../features/dashboard/dashboard.component';
import { CursusListComponent } from '../features/cursus/pages/cursus-list/cursus-list.component';
import { FiliereListComponent } from '../features/filieres/pages/filiere-list/filiere-list.component';
import { InscriptionListComponent } from '../features/inscriptions/pages/inscription-list/inscription-list.component';
import { PromotionListComponent } from '../features/promotions/pages/promotion-list/promotion-list.component';
import { UserListComponent } from '../features/utilisateurs/pages/user-list/user-list.component';
import { MainLayoutComponent } from '../shared/components/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard],
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', canActivate: [defaultRouteGuard], children: [] },
      {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [roleGuard('TEACHER', 'REFERENTE', 'STUDENT', 'ADMIN')],
      },
      {
        path: 'cours',
        component: CoursListComponent,
        canActivate: [roleGuard('REFERENTE', 'TEACHER', 'ADMIN')],
      },
      {
        path: 'promotions',
        component: PromotionListComponent,
        canActivate: [roleGuard('REFERENTE', 'ADMIN')],
      },
      {
        path: 'calendrier',
        component: CalendrierComponent,
        canActivate: [roleGuard('STUDENT')],
      },
      {
        path: 'filieres',
        component: FiliereListComponent,
        canActivate: [roleGuard('REFERENTE', 'ADMIN')],
      },
      {
        path: 'cursus',
        component: CursusListComponent,
        canActivate: [roleGuard('REFERENTE', 'ADMIN')],
      },
      {
        path: 'inscriptions',
        component: InscriptionListComponent,
        canActivate: [roleGuard('REFERENTE', 'ADMIN')],
      },
      {
        path: 'utilisateurs',
        component: UserListComponent,
        canActivate: [roleGuard('ADMIN')],
      },
      {
        path: 'audit',
        component: AuditListComponent,
        canActivate: [roleGuard('ADMIN')],
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
