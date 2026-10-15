// [Benjamin – #07, 2026-10-15] Création. Miroir des DTO Java EnrollmentRequest / EnrollmentResponse / StudentDTO / PlanningItem.
export type EnrollmentType = 'PROMOTION' | 'UNIT';

export interface Enrollment {
  id: number;
  studentId: number;
  studentName: string;
  type: EnrollmentType;
  promotionId: number | null;
  promotionName: string | null;
  courseId: number | null;
  courseTitle: string | null;
  enrollmentDate: string;
  forced: boolean;
}

export interface EnrollmentRequest {
  studentId: number;
  type: EnrollmentType;
  promotionId: number | null;
  courseId: number | null;
  forced: boolean;
}

export interface Student {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface PlanningItem {
  promotionName: string | null;
  ordre: number | null;
  courseCode: string;
  courseTitle: string;
  startDate: string | null;
  endDate: string | null;
}
