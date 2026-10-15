import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { fakeJwt } from '../../../testing/jwt';
import { AuthService } from './auth.service';

// [Benjamin – #09, 2026-10-15] Création.
describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('appelle /api/auth/login et mémorise le token et l\'utilisateur', () => {
    const token = fakeJwt(3600);
    let role = '';
    service.login('referente@educmanager.fr', 'referente123').subscribe((u) => (role = u.role));

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'referente@educmanager.fr', password: 'referente123' });
    req.flush({ token, role: 'REFERENTE', userId: 2, username: 'referente@educmanager.fr', firstName: 'Sophie', lastName: 'Leroy' });

    expect(role).toBe('REFERENTE');
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.getToken()).toBe(token);
    expect(service.getUser()?.prenom).toBe('Sophie');
    expect(service.getDefaultRoute()).toBe('/filieres');
  });

  it('propage l\'erreur 401 et reste déconnecté', () => {
    let status = 0;
    service.login('x@y.fr', 'faux').subscribe({ error: (e) => (status = e.status) });
    http.expectOne('/api/auth/login').flush({ message: 'Email ou mot de passe incorrect.' }, { status: 401, statusText: 'Unauthorized' });

    expect(status).toBe(401);
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('considère un token expiré comme une session terminée', () => {
    localStorage.setItem('token', fakeJwt(-60));
    localStorage.setItem('user', JSON.stringify({ email: 'a@b.fr', nom: 'B', prenom: 'A', role: 'STUDENT' }));
    const fresh = new AuthService(TestBed.inject(HttpClient));

    expect(fresh.getToken()).toBeNull();
    expect(fresh.isLoggedIn()).toBeFalse();
  });

  it('logout efface la session', () => {
    localStorage.setItem('token', fakeJwt(3600));
    service.logout();
    expect(localStorage.getItem('token')).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });
});
