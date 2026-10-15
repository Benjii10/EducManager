import { UserRole } from './user-role.model';

export interface User {
  id?: number; // [Benjamin – #01] id du UserAccount côté back
  email: string;
  nom: string;
  prenom: string;
  role: UserRole;
}

/**
 * [Benjamin – #01, 2026-10-15] Réponse de POST /api/auth/login
 * (miroir du DTO Java com.educmanager.dto.LoginResponse).
 */
export interface LoginResponse {
  token: string;
  role: UserRole;
  userId: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
}
