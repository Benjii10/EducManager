import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../../../../core/auth/auth.service';
import { UserService } from '../../services/user.service';
import { UserListComponent } from './user-list.component';

// [Benjamin – #09, 2026-10-15] Création.
describe('UserListComponent', () => {
  let users: jasmine.SpyObj<UserService>;

  beforeEach(() => {
    users = jasmine.createSpyObj<UserService>('UserService', ['getAll', 'create', 'update', 'delete']);
    users.getAll.and.returnValue(of([]));
    const auth = jasmine.createSpyObj<AuthService>('AuthService', ['getUser']);
    auth.getUser.and.returnValue({ email: 'admin@educmanager.fr', nom: 'D', prenom: 'A', role: 'ADMIN' });
    TestBed.configureTestingModule({
      imports: [UserListComponent],
      providers: [
        { provide: UserService, useValue: users },
        { provide: AuthService, useValue: auth },
      ],
    });
  });

  it('applique la même règle de mot de passe que le backend', () => {
    const c = TestBed.createComponent(UserListComponent).componentInstance;
    c.form.password = 'court1';
    expect(c.passwordInvalid).toBeTrue();
    c.form.password = 'sanschiffre';
    expect(c.passwordInvalid).toBeTrue();
    c.form.password = 'motdepasse1';
    expect(c.passwordInvalid).toBeFalse();
  });

  it('accepte un mot de passe vide en modification (inchangé)', () => {
    const c = TestBed.createComponent(UserListComponent).componentInstance;
    c.edit({ id: 3, email: 'p@t.fr', firstName: 'P', lastName: 'T', role: 'TEACHER', studentId: null });
    expect(c.passwordInvalid).toBeFalse();
  });

  it('reconnaît le compte connecté (suppression désactivée)', () => {
    const c = TestBed.createComponent(UserListComponent).componentInstance;
    expect(c.isMe({ id: 1, email: 'admin@educmanager.fr', firstName: 'A', lastName: 'D', role: 'ADMIN', studentId: null })).toBeTrue();
  });
});
