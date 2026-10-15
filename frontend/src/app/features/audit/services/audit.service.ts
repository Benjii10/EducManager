import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { AuditLog, Page } from '../../../shared/models/audit-log.model';

// [Benjamin – #07, 2026-10-15] Création. Lecture du journal d'audit (MongoDB) — ADMIN.
@Injectable({
  providedIn: 'root',
})
export class AuditService {
  private url = environment.apiUrl + '/audit';

  constructor(private http: HttpClient) {}

  search(username: string, action: string, page: number, size = 20): Observable<Page<AuditLog>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (username.trim()) {
      params = params.set('username', username.trim());
    }
    if (action) {
      params = params.set('action', action);
    }
    return this.http.get<Page<AuditLog>>(this.url, { params });
  }
}
