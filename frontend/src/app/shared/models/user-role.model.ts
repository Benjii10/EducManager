// [Benjamin – #01, 2026-10-15] Rôles alignés sur l'enum Java com.educmanager.enums.Role
// (avant : FORMATEUR / ETUDIANT côté front, TEACHER / STUDENT côté back → incompatibles avec le JWT).
export type UserRole = 'ADMIN' | 'TEACHER' | 'REFERENTE' | 'STUDENT';

export const USER_ROLES: UserRole[] = ['ADMIN', 'TEACHER', 'REFERENTE', 'STUDENT'];

/** Libellés affichés à l'écran (le code technique reste en anglais, comme dans le back). */
export const ROLE_LABELS: Record<UserRole, string> = {
  ADMIN: 'Administrateur',
  TEACHER: 'Formateur',
  REFERENTE: 'Référente',
  STUDENT: 'Étudiant',
};
