import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../../../core/auth/auth.service';
import { ManagedUser, UserForm } from '../../../../shared/models/managed-user.model';
import { ROLE_LABELS, USER_ROLES, UserRole } from '../../../../shared/models/user-role.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { UserService } from '../../services/user.service';

/**
 * Gestion des comptes utilisateurs (ADMIN).
 * [Benjamin – #07, 2026-10-15] Création (remplace la page « bientôt disponible »).
 */
@Component({
  selector: 'app-user-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './user-list.component.html',
})
export class UserListComponent implements OnInit {
  readonly roles = USER_ROLES;
  readonly roleLabels = ROLE_LABELS;

  users: ManagedUser[] = [];
  form: UserForm = this.emptyForm();
  editingId: number | null = null;
  loading = true;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.load();
  }

  isMe(u: ManagedUser): boolean {
    return this.authService.getUser()?.email === u.email;
  }

  /** Même règle que le backend : 8 caractères min., au moins une lettre et un chiffre. */
  get passwordInvalid(): boolean {
    const p = this.form.password;
    if (this.editingId !== null && !p) {
      return false; // mot de passe inchangé
    }
    return !(p.length >= 8 && /[A-Za-z]/.test(p) && /\d/.test(p));
  }

  load(): void {
    this.loading = true;
    this.userService.getAll().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiErrorMessage(err);
        this.loading = false;
      },
    });
  }

  save(): void {
    if (!this.form.firstName.trim() || !this.form.lastName.trim() || (this.editingId === null && !this.form.email?.trim())) {
      this.error = 'Email, prénom et nom sont obligatoires.';
      return;
    }
    if (this.passwordInvalid) {
      this.error = 'Mot de passe : 8 caractères minimum, avec au moins une lettre et un chiffre.';
      return;
    }
    this.error = null;
    this.success = null;
    const request =
      this.editingId === null ? this.userService.create(this.form) : this.userService.update(this.editingId, this.form);
    request.subscribe({
      next: (u) => {
        this.success = `Compte ${u.email} ${this.editingId === null ? 'créé' : 'modifié'}.`;
        this.cancel();
        this.load();
      },
      error: (err) => (this.error = apiErrorMessage(err)),
    });
  }

  edit(u: ManagedUser): void {
    this.editingId = u.id;
    this.form = { email: u.email, firstName: u.firstName, lastName: u.lastName, role: u.role, password: '' };
  }

  cancel(): void {
    this.editingId = null;
    this.form = this.emptyForm();
  }

  remove(u: ManagedUser): void {
    if (!confirm(`Supprimer le compte ${u.email} ?`)) {
      return;
    }
    this.userService.delete(u.id).subscribe({
      next: () => {
        this.success = 'Compte supprimé.';
        this.load();
      },
      error: (err) => (this.error = apiErrorMessage(err)),
    });
  }

  private emptyForm(): UserForm {
    return { email: '', firstName: '', lastName: '', role: 'STUDENT' as UserRole, password: '' };
  }
}
