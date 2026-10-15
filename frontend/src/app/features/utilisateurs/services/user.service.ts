import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { ManagedUser, UserForm } from '../../../shared/models/managed-user.model';

// [Benjamin – #07, 2026-10-15] Création. Gestion des comptes (ADMIN).
@Injectable({
  providedIn: 'root',
})
export class UserService {
  private url = environment.apiUrl + '/users';

  constructor(private http: HttpClient) {}

  getAll(): Observable<ManagedUser[]> {
    return this.http.get<ManagedUser[]>(this.url);
  }

  create(user: UserForm): Observable<ManagedUser> {
    return this.http.post<ManagedUser>(this.url, user);
  }

  update(id: number, user: UserForm): Observable<ManagedUser> {
    return this.http.put<ManagedUser>(`${this.url}/${id}`, user);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
