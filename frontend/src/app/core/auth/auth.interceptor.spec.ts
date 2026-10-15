import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { fakeJwt } from '../../../testing/jwt';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

// [Benjamin – #09, 2026-10-15] Création.
describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.clear();
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => localStorage.clear());

  it('ajoute le token Bearer sur les appels à l\'API', () => {
    const token = fakeJwt(3600);
    localStorage.setItem('token', token);

    http.get('/api/filieres').subscribe();
    const req = backend.expectOne('/api/filieres');

    expect(req.request.headers.get('Authorization')).toBe('Bearer ' + token);
  });

  it('n\'ajoute rien sur un appel externe', () => {
    localStorage.setItem('token', fakeJwt(3600));
    http.get('https://exemple.fr/data').subscribe();
    expect(backend.expectOne('https://exemple.fr/data').request.headers.has('Authorization')).toBeFalse();
  });

  it('déconnecte et renvoie au login sur une réponse 401', () => {
    localStorage.setItem('token', fakeJwt(3600));
    const auth = TestBed.inject(AuthService);
    spyOn(auth, 'logout').and.callThrough();

    http.get('/api/cours').subscribe({ error: () => undefined });
    backend.expectOne('/api/cours').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('ne redirige pas sur un 401 du login lui-même', () => {
    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });
    backend.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
