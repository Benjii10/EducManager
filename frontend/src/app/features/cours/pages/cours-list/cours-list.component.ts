import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../../../core/auth/auth.service';
import { Course } from '../../../../shared/models/course.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { CourseService } from '../../services/course.service';

/**
 * Liste et gestion des cours.
 * [Benjamin – #07, 2026-10-15] Réécriture : données réelles de l'API (avant : données fictives),
 * création / modification / suppression pour REFERENTE et ADMIN, lecture seule pour TEACHER.
 */
@Component({
  selector: 'app-cours-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './cours-list.component.html',
})
export class CoursListComponent implements OnInit {
  courses: Course[] = [];
  loading = true;
  error: string | null = null;
  success: string | null = null;
  searchQuery = '';

  form: Course = this.emptyForm();
  editingId: number | null = null;

  constructor(
    private courseService: CourseService,
    private authService: AuthService,
  ) {}

  /** Seules la référente et l'admin peuvent modifier (même règle que le backend). */
  get canEdit(): boolean {
    return this.authService.hasRole('REFERENTE', 'ADMIN');
  }

  get filteredCourses(): Course[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) {
      return this.courses;
    }
    return this.courses.filter(
      (c) => c.titre.toLowerCase().includes(query) || c.code.toLowerCase().includes(query),
    );
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.courseService.getAll().subscribe({
      next: (data) => {
        this.courses = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiErrorMessage(err, 'Impossible de charger les cours.');
        this.loading = false;
      },
    });
  }

  save(): void {
    if (!this.form.code.trim() || !this.form.titre.trim()) {
      this.error = 'Le code et le titre sont obligatoires.';
      return;
    }
    this.error = null;
    this.success = null;
    const request =
      this.editingId === null
        ? this.courseService.create(this.form)
        : this.courseService.update(this.editingId, this.form);

    request.subscribe({
      next: () => {
        this.success = this.editingId === null ? 'Cours créé.' : 'Cours modifié.';
        this.cancel();
        this.load();
      },
      error: (err) => (this.error = apiErrorMessage(err)),
    });
  }

  edit(course: Course): void {
    this.editingId = course.id ?? null;
    this.form = { ...course };
  }

  cancel(): void {
    this.editingId = null;
    this.form = this.emptyForm();
  }

  remove(course: Course): void {
    if (course.id === undefined || !confirm(`Supprimer le cours « ${course.titre} » ?`)) {
      return;
    }
    this.error = null;
    this.courseService.delete(course.id).subscribe({
      next: () => {
        this.success = 'Cours supprimé.';
        this.load();
      },
      error: (err) => (this.error = apiErrorMessage(err)),
    });
  }

  private emptyForm(): Course {
    return { code: '', titre: '', description: '', dureeJours: 1 };
  }
}
