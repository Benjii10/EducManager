import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Promotion } from '../../../shared/models/promotion.model';

// [Benjamin – #07, 2026-10-15] Création.
@Injectable({
  providedIn: 'root',
})
export class PromotionService {
  private url = environment.apiUrl + '/promotions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(this.url);
  }

  create(promotion: Promotion): Observable<Promotion> {
    return this.http.post<Promotion>(this.url, promotion);
  }

  update(id: number, promotion: Promotion): Observable<Promotion> {
    return this.http.put<Promotion>(`${this.url}/${id}`, promotion);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
