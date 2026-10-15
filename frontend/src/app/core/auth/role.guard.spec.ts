import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';

import { AuthService } from './auth.service';
import { roleGuard } from './role.guard';

// [Benjamin – #09, 2026-10-15] Création.
describe('roleGuard', () => {
  let auth: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const run = (...roles: Parameters<typeof roleGuard>) =>
    TestBed.runInInjectionContext(() =>
      roleGuard(...roles)({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  beforeEach(() => {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole', 'getDefaultRoute']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });
  });

  it('laisse passer un rôle autorisé', () => {
    auth.hasRole.and.returnValue(true);
    expect(run('ADMIN')).toBeTrue();
  });

  it('redirige un rôle non autorisé vers sa page par défaut', () => {
    auth.hasRole.and.returnValue(false);
    auth.getDefaultRoute.and.returnValue('/calendrier');

    expect(run('ADMIN')).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/calendrier']);
  });
});
