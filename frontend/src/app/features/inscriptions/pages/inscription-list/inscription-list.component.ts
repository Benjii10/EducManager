import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CourseService } from '../../../cours/services/course.service';
import { PromotionService } from '../../../promotions/services/promotion.service';
import { Course } from '../../../../shared/models/course.model';
import { Enrollment, EnrollmentRequest, Student } from '../../../../shared/models/enrollment.model';
import { Promotion } from '../../../../shared/models/promotion.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { EnrollmentService } from '../../services/enrollment.service';

/**
 * Inscriptions des élèves à une promotion ou à un cours à l'unité (REFERENTE, ADMIN).
 * [Benjamin – #07, 2026-10-15] Création (remplace la page « bientôt disponible »).
 */
@Component({
  selector: 'app-inscription-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './inscription-list.component.html',
})
export class InscriptionListComponent implements OnInit {
  enrollments: Enrollment[] = [];
  students: Student[] = [];
  promotions: Promotion[] = [];
  courses: Course[] = [];
  form: EnrollmentRequest = this.emptyForm();
  loading = true;
  error: string | null = null;
  success: string | null = null;
  filter = '';

  constructor(
    private enrollmentService: EnrollmentService,
    private promotionService: PromotionService,
    private courseService: CourseService,
  ) {}

  ngOnInit(): void {
    this.enrollmentService.getStudents().subscribe({ next: (d) => (this.students = d), error: (e) => this.fail(e) });
    this.promotionService.getAll().subscribe({ next: (d) => (this.promotions = d), error: (e) => this.fail(e) });
    this.courseService.getAll().subscribe({ next: (d) => (this.courses = d), error: (e) => this.fail(e) });
    this.load();
  }

  get filtered(): Enrollment[] {
    const q = this.filter.trim().toLowerCase();
    return q
      ? this.enrollments.filter(
          (e) =>
            e.studentName.toLowerCase().includes(q) ||
            (e.promotionName ?? '').toLowerCase().includes(q) ||
            (e.courseTitle ?? '').toLowerCase().includes(q),
        )
      : this.enrollments;
  }

  load(): void {
    this.loading = true;
    this.enrollmentService.getAll().subscribe({
      next: (data) => {
        this.enrollments = data;
        this.loading = false;
      },
      error: (err) => {
        this.fail(err);
        this.loading = false;
      },
    });
  }

  onTypeChange(): void {
    this.form.promotionId = null;
    this.form.courseId = null;
  }

  enroll(): void {
    const target = this.form.type === 'PROMOTION' ? this.form.promotionId : this.form.courseId;
    if (!this.form.studentId || !target) {
      this.error = "Choisissez un élève et une promotion (ou un cours).";
      return;
    }
    this.error = null;
    this.success = null;
    this.enrollmentService.enroll(this.form).subscribe({
      next: (e) => {
        this.success = `${e.studentName} inscrit(e) à ${e.promotionName ?? e.courseTitle}.`;
        this.form = this.emptyForm();
        this.load();
      },
      error: (err) => this.fail(err),
    });
  }

  remove(e: Enrollment): void {
    if (!confirm(`Désinscrire ${e.studentName} ?`)) {
      return;
    }
    this.enrollmentService.delete(e.id).subscribe({
      next: () => {
        this.success = 'Inscription supprimée.';
        this.load();
      },
      error: (err) => this.fail(err),
    });
  }

  private fail(err: unknown): void {
    this.error = apiErrorMessage(err);
  }

  private emptyForm(): EnrollmentRequest {
    return { studentId: 0, type: 'PROMOTION', promotionId: null, courseId: null, forced: false };
  }
}
