// [Benjamin – #01, 2026-10-15] rôles renommés : FORMATEUR → TEACHER, ETUDIANT → STUDENT (alignés sur le back)
// [Benjamin – #07, 2026-10-15] ajout du Journal d'audit, accès ADMIN au référentiel
import { UserRole } from '../models/user-role.model';

export interface NavItem {
  label: string;
  path: string;
  roles: UserRole[];
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Tableau de bord', path: '/dashboard', roles: ['TEACHER', 'REFERENTE', 'STUDENT', 'ADMIN'] },
  { label: 'Mon planning', path: '/calendrier', roles: ['STUDENT'] },
  { label: 'Filières', path: '/filieres', roles: ['REFERENTE', 'ADMIN'] },
  { label: 'Cursus', path: '/cursus', roles: ['REFERENTE', 'ADMIN'] },
  { label: 'Cours', path: '/cours', roles: ['REFERENTE', 'TEACHER', 'ADMIN'] },
  { label: 'Promotions', path: '/promotions', roles: ['REFERENTE', 'ADMIN'] },
  { label: 'Inscriptions', path: '/inscriptions', roles: ['REFERENTE', 'ADMIN'] },
  { label: 'Utilisateurs', path: '/utilisateurs', roles: ['ADMIN'] },
  { label: "Journal d'audit", path: '/audit', roles: ['ADMIN'] },
];
