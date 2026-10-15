import { UserRole } from './user-role.model';

// [Benjamin – #07, 2026-10-15] Création. Miroir des DTO Java UserResponse / CreateUserRequest / UpdateUserRequest.
export interface ManagedUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  studentId: number | null;
}

export interface UserForm {
  email?: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  password: string;
}
