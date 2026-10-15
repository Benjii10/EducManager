import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Enrollment, EnrollmentRequest, PlanningItem, Student } from '../../../shared/models/enrollment.model';

// [Benjamin – #07, 2026-10-15] Création.
@Injectable({
  providedIn: 'root',
})
export class EnrollmentService {
  private url = environment.apiUrl + '/enrollment';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(this.url);
  }

  enroll(request: EnrollmentRequest): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.url, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  getStudents(): Observable<Student[]> {
    return this.http.get<Student[]>(environment.apiUrl + '/student');
  }

  /** Inscriptions de l'élève connecté. */
  getMine(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.url}/me`);
  }

  /** Planning calculé de l'élève connecté. */
  getMyPlanning(): Observable<PlanningItem[]> {
    return this.http.get<PlanningItem[]>(`${this.url}/me/planning`);
  }
}
