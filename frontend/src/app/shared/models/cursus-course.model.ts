import { Course } from './course.model';

// [Benjamin – #07, 2026-10-15] Création. Association ordonnée cursus ↔ cours (entité Java CursusCourse).
export interface CursusCourse {
  id: number;
  ordre: number;
  course: Course;
}
