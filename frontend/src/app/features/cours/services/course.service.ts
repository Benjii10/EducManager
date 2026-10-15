import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { Course } from '../../../shared/models/course.model';
import { CursusCourse } from '../../../shared/models/cursus-course.model';

// [Benjamin – #07, 2026-10-15] Remplacement des données fictives (courses.mock.ts) par l'API /api/courses
// + gestion des cours d'un cursus (/api/cursus-courses).
@Injectable({
  providedIn: 'root',
})
export class CourseService {
  private url = environment.apiUrl + '/courses';
  private cursusCoursesUrl = environment.apiUrl + '/cursus-courses';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Course[]> {
    return this.http.get<Course[]>(this.url);
  }

  create(course: Course): Observable<Course> {
    return this.http.post<Course>(this.url, course);
  }

  update(id: number, course: Course): Observable<Course> {
    return this.http.put<Course>(`${this.url}/${id}`, course);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  getByCursus(cursusId: number): Observable<CursusCourse[]> {
    return this.http.get<CursusCourse[]>(this.cursusCoursesUrl, { params: { cursusId } });
  }

  addToCursus(cursusId: number, courseId: number, ordre: number): Observable<CursusCourse> {
    return this.http.post<CursusCourse>(this.cursusCoursesUrl, { cursusId, courseId, ordre });
  }

  removeFromCursus(cursusCourseId: number): Observable<void> {
    return this.http.delete<void>(`${this.cursusCoursesUrl}/${cursusCourseId}`);
  }
}
