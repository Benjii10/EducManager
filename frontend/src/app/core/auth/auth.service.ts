import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UserRole } from '../../shared/models/user-role.model';
import { LoginResponse, User } from '../../shared/models/user.model';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

/**
 * Gestion de la session utilisateur.
 *
 * [Benjamin – #01, 2026-10-15] Remplacement des comptes écrits en dur par l'API :
 *  - login() appelle POST /api/auth/login et stocke le token JWT + l'utilisateur ;
 *  - le token est ajouté à chaque requête par authInterceptor (core/auth/auth.interceptor.ts) ;
 *  - isLoggedIn() vérifie aussi que le token n'est pas expiré.
 * Les guards (auth, guest, role, default-route) n'ont pas changé : ils s'appuient sur ce service.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private currentUser: User | null = null;
  private readonly loginUrl = environment.apiUrl + '/auth/login';

  constructor(private http: HttpClient) {
    const saved = localStorage.getItem(USER_KEY);
    if (saved && this.getToken()) {
      this.currentUser = JSON.parse(saved);
    }
  }

  /** Appelle l'API ; émet l'utilisateur connecté, ou une erreur HTTP 401 si identifiants faux. */
  login(email: string, password: string): Observable<User> {
    return this.http.post<LoginResponse>(this.loginUrl, { username: email, password }).pipe(
      map((res) => ({
        response: res,
        user: {
          id: res.userId,
          email: res.username,
          prenom: res.firstName ?? '',
          nom: res.lastName ?? '',
          role: res.role,
        } as User,
      })),
      tap(({ response, user }) => {
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUser = user;
      }),
      map(({ user }) => user),
    );
  }

  logout(): void {
    this.currentUser = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  getToken(): string | null {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token || this.isExpired(token)) {
      return null;
    }
    return token;
  }

  isLoggedIn(): boolean {
    if (this.currentUser !== null && this.getToken() === null) {
      this.logout(); // token expiré : on nettoie la session
    }
    return this.currentUser !== null;
  }

  getUser(): User | null {
    return this.currentUser;
  }

  hasRole(...roles: UserRole[]): boolean {
    const user = this.getUser();
    return user !== null && roles.includes(user.role);
  }

  getDefaultRoute(): string {
    const user = this.getUser();
    if (!user) {
      return '/login';
    }
    switch (user.role) {
      case 'TEACHER':
        return '/cours';
      case 'ADMIN':
        return '/utilisateurs';
      case 'REFERENTE':
        return '/filieres';
      case 'STUDENT':
        return '/calendrier';
      default:
        return '/dashboard';
    }
  }

  /** Lit la date d'expiration (claim "exp", en secondes) dans la partie centrale du JWT. */
  private isExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' && payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
