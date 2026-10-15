import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CursusService } from '../../../cursus/services/cursus.service';
import { Cursus } from '../../../../shared/models/cursus.model';
import { Promotion } from '../../../../shared/models/promotion.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { PromotionService } from '../../services/promotion.service';

/**
 * Gestion des promotions (REFERENTE, ADMIN).
 * [Benjamin – #07, 2026-10-15] Création (remplace la page « bientôt disponible »).
 */
@Component({
  selector: 'app-promotion-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './promotion-list.component.html',
})
export class PromotionListComponent implements OnInit {
  promotions: Promotion[] = [];
  cursus: Cursus[] = [];
  form: Promotion = this.emptyForm();
  editingId: number | null = null;
  loading = true;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private promotionService: PromotionService,
    private cursusService: CursusService,
  ) {}

  ngOnInit(): void {
    this.cursusService.getAll().subscribe({
      next: (data) => (this.cursus = data),
      error: (err) => (this.error = apiErrorMessage(err, 'Impossible de charger les cursus.')),
    });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.promotionService.getAll().subscribe({
      next: (data) => {
        this.promotions = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiErrorMessage(err, 'Impossible de charger les promotions.');
        this.loading = false;
      },
    });
  }

  /** Contrôle côté client (le backend refait la même vérification). */
  get datesInvalides(): boolean {
    return !!this.form.startDate && !!this.form.endDate && this.form.endDate < this.form.startDate;
  }

  save(): void {
    if (!this.form.name.trim() || !this.form.startDate || !this.form.endDate || !this.form.cursusId) {
      this.error = 'Tous les champs sont obligatoires.';
      return;
    }
    if (this.datesInvalides) {
      this.error = 'La date de fin doit être postérieure à la date de début.';
      return;
    }
    this.error = null;
    this.success = null;
    const request =
      this.editingId === null
        ? this.promotionService.create(this.form)
        : this.promotionService.update(this.editingId, this.form);
    request.subscribe({
      next: () => {
        this.success = this.editingId === null ? 'Promotion créée.' : 'Promotion modifiée.';
        this.cancel();
        this.load();
      },
      error: (err) => (this.error = apiErrorMessage(err)),
    });
  }

  edit(p: Promotion): void {
    this.editingId = p.id ?? null;
    this.form = { ...p };
  }

  cancel(): void {
    this.editingId = null;
    this.form = this.emptyForm();
  }

  remove(p: Promotion): void {
    if (p.id === undefined || !confirm(`Supprimer la promotion « ${p.name} » ?`)) {
      return;
    }
    this.promotionService.delete(p.id).subscribe({
      next: () => {
        this.success = 'Promotion supprimée.';
        this.load();
      },
      error: (err) => (this.error = apiErrorMessage(err)),
    });
  }

  private emptyForm(): Promotion {
    return { name: '', startDate: '', endDate: '', cursusId: 0 };
  }
}
