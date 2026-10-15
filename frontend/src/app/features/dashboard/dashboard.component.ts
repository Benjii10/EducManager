import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { CourseService } from '../cours/services/course.service';
import { CursusService } from '../cursus/services/cursus.service';
import { FiliereService } from '../filieres/services/filiere.service';
import { CalendrierService } from '../calendrier/services/calendrier.service';
import { PromotionService } from '../promotions/services/promotion.service';
import { PlanningItem } from '../../shared/models/enrollment.model';
import { ROLE_LABELS } from '../../shared/models/user-role.model';

/**
 * Tableau de bord.
 * [Benjamin – #07, 2026-10-15] Contenu réel : chiffres clés pour l'équipe pédagogique,
 * prochain cours pour l'élève (avant : « Contenu à compléter »).
 */
@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <h1>Tableau de bord</h1>
      <p>Bienvenue, {{ userName }} <span class="badge">{{ roleLabel }}</span></p>

      <div *ngIf="stats" class="tiles">
        <a class="tile" routerLink="/filieres" *ngIf="isStaff"><strong>{{ stats.filieres }}</strong> filières</a>
        <a class="tile" routerLink="/cursus" *ngIf="isStaff"><strong>{{ stats.cursus }}</strong> cursus</a>
        <a class="tile" routerLink="/cours"><strong>{{ stats.cours }}</strong> cours</a>
        <a class="tile" routerLink="/promotions" *ngIf="isStaff"><strong>{{ stats.promotions }}</strong> promotions</a>
      </div>

      <div *ngIf="isStudent">
        <h2>Prochain cours</h2>
        <p *ngIf="next; else rien">
          <strong>{{ next.courseTitle }}</strong> du {{ next.startDate | date: 'dd/MM/yyyy' }}
          au {{ next.endDate | date: 'dd/MM/yyyy' }} ({{ next.promotionName }})
        </p>
        <ng-template #rien><p class="muted">Aucun cours à venir.</p></ng-template>
        <a routerLink="/calendrier">Voir mon planning complet</a>
      </div>
    </section>
  `,
  styles: `
    .tiles { display: flex; flex-wrap: wrap; gap: 16px; margin-top: 16px; }
    .tile { display: block; min-width: 140px; padding: 16px; background: #fff; border: 1px solid #d0d0d0;
            border-radius: 6px; color: inherit; text-decoration: none; }
    .tile strong { display: block; font-size: 28px; color: #1d4ed8; }
  `,
})
export class DashboardComponent implements OnInit {
  stats: { filieres: number; cursus: number; cours: number; promotions: number } | null = null;
  next: PlanningItem | null = null;

  constructor(
    private authService: AuthService,
    private filiereService: FiliereService,
    private cursusService: CursusService,
    private courseService: CourseService,
    private promotionService: PromotionService,
    private calendrierService: CalendrierService,
  ) {}

  get userName(): string {
    const user = this.authService.getUser();
    return user ? `${user.prenom} ${user.nom}` : '';
  }

  get roleLabel(): string {
    const user = this.authService.getUser();
    return user ? ROLE_LABELS[user.role] : '';
  }

  get isStaff(): boolean {
    return this.authService.hasRole('REFERENTE', 'ADMIN');
  }

  get isStudent(): boolean {
    return this.authService.hasRole('STUDENT');
  }

  ngOnInit(): void {
    if (this.isStudent) {
      const today = new Date().toISOString().substring(0, 10);
      this.calendrierService.getPlanning().subscribe((items) => {
        this.next = items.find((i) => i.endDate !== null && i.endDate >= today) ?? null;
      });
      return;
    }
    forkJoin({
      filieres: this.filiereService.getAll(),
      cursus: this.cursusService.getAll(),
      cours: this.courseService.getAll(),
      promotions: this.promotionService.getAll(),
    }).subscribe((r) => {
      this.stats = {
        filieres: r.filieres.length,
        cursus: r.cursus.length,
        cours: r.cours.length,
        promotions: r.promotions.length,
      };
    });
  }
}
