import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { PlanningItem } from '../../../shared/models/enrollment.model';

// [Benjamin – #07, 2026-10-15] Remplacement des données fictives par le planning calculé par l'API
// (GET /api/enrollment/me/planning).
@Injectable({
  providedIn: 'root',
})
export class CalendrierService {
  constructor(private http: HttpClient) {}

  getPlanning(): Observable<PlanningItem[]> {
    return this.http.get<PlanningItem[]>(environment.apiUrl + '/enrollment/me/planning');
  }
}
