import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CourseService } from '../../../cours/services/course.service';
import { FiliereService } from '../../../filieres/services/filiere.service';
import { Course } from '../../../../shared/models/course.model';
import { CursusCourse } from '../../../../shared/models/cursus-course.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { Cursus } from '../../../../shared/models/cursus.model';
import { Filiere } from '../../../../shared/models/filiere.model';
import { CursusService } from '../../services/cursus.service';

@Component({
  selector: 'app-cursus-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './cursus-list.component.html',
  styleUrl: './cursus-list.component.scss',
})
export class CursusListComponent implements OnInit {
  cursus: Cursus[] = [];
  filieres: Filiere[] = [];
  name = '';
  filiereId: number | null = null;
  editingCursusId: number | null = null;
  editingName = '';
  editingFiliereId: number | null = null;
  loading = true;
  error: string | null = null;

  // [Benjamin – #07, 2026-10-15] Gestion des cours d'un cursus (ordre du parcours)
  selectedCursus: Cursus | null = null;
  cursusCourses: CursusCourse[] = [];
  allCourses: Course[] = [];
  courseToAdd: number | null = null;
  courseError: string | null = null;

  constructor(
    private cursusService: CursusService,
    private filiereService: FiliereService,
    private courseService: CourseService,
  ) {}

  /** Cours pas encore présents dans le cursus sélectionné. */
  get availableCourses(): Course[] {
    const used = new Set(this.cursusCourses.map((cc) => cc.course.id));
    return this.allCourses.filter((c) => !used.has(c.id));
  }

  openCourses(cursus: Cursus): void {
    this.selectedCursus = cursus;
    this.courseError = null;
    this.courseToAdd = null;
    this.courseService.getAll().subscribe({ next: (d) => (this.allCourses = d) });
    this.loadCursusCourses();
  }

  closeCourses(): void {
    this.selectedCursus = null;
    this.cursusCourses = [];
  }

  loadCursusCourses(): void {
    if (!this.selectedCursus?.id) {
      return;
    }
    this.courseService.getByCursus(this.selectedCursus.id).subscribe({
      next: (d) => (this.cursusCourses = d),
      error: (err) => (this.courseError = apiErrorMessage(err)),
    });
  }

  addCourse(): void {
    if (!this.selectedCursus?.id || this.courseToAdd === null) {
      return;
    }
    const nextOrder = this.cursusCourses.reduce((max, cc) => Math.max(max, cc.ordre), 0) + 1;
    this.courseService.addToCursus(this.selectedCursus.id, this.courseToAdd, nextOrder).subscribe({
      next: () => {
        this.courseToAdd = null;
        this.loadCursusCourses();
      },
      error: (err) => (this.courseError = apiErrorMessage(err)),
    });
  }

  removeCourse(cc: CursusCourse): void {
    this.courseService.removeFromCursus(cc.id).subscribe({
      next: () => this.loadCursusCourses(),
      error: (err) => (this.courseError = apiErrorMessage(err)),
    });
  }

  ngOnInit(): void {
    this.loadFilieres();
    this.loadCursus();
  }

  loadCursus(): void {
    this.loading = true;
    this.error = null;

    this.cursusService.getAll().subscribe({
      next: (data) => {
        this.cursus = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les cursus.';
        this.loading = false;
      },
    });
  }

  loadFilieres(): void {
    this.filiereService.getAll().subscribe({
      next: (data) => {
        this.filieres = data;
      },
      error: () => {
        this.error = 'Impossible de charger les filieres.';
      },
    });
  }

  onSubmit(): void {
    const name = this.name.trim();
    if (!name || this.filiereId === null) {
      return;
    }

    this.error = null;

    this.cursusService.create({ name, filiereId: this.filiereId }).subscribe({
      next: () => {
        this.name = '';
        this.filiereId = null;
        this.loadCursus();
      },
      error: () => {
        this.error = 'Impossible de creer le cursus.';
      },
    });
  }

  startEdit(cursus: Cursus): void {
    if (cursus.id === undefined) {
      return;
    }

    this.editingCursusId = cursus.id;
    this.editingName = cursus.name;
    this.editingFiliereId = cursus.filiereId;
  }

  cancelEdit(): void {
    this.editingCursusId = null;
    this.editingName = '';
    this.editingFiliereId = null;
  }

  updateCursus(): void {
    if (this.editingCursusId === null) {
      return;
    }

    const name = this.editingName.trim();
    if (!name || this.editingFiliereId === null) {
      return;
    }

    this.error = null;

    this.cursusService.update(this.editingCursusId, {
      name,
      filiereId: this.editingFiliereId,
    }).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadCursus();
      },
      error: () => {
        this.error = 'Impossible de modifier le cursus.';
      },
    });
  }

  deleteCursus(id: number): void {
    if (!confirm('Supprimer ce cursus ?')) {
      return;
    }

    this.error = null;

    this.cursusService.delete(id).subscribe({
      next: () => {
        if (this.editingCursusId === id) {
          this.cancelEdit();
        }
        this.loadCursus();
      },
      error: () => {
        this.error = 'Impossible de supprimer le cursus.';
      },
    });
  }
}
