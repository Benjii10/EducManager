import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import { PlanningItem } from '../../../../shared/models/enrollment.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { CalendrierService } from '../../services/calendrier.service';

interface MoisGroupe {
  label: string;
  items: PlanningItem[];
}

/**
 * Planning de l'élève connecté.
 * [Benjamin – #07, 2026-10-15] Réécriture : planning réel calculé par l'API à partir de ses
 * inscriptions (avant : données fictives), regroupé par mois ; cours à l'unité listés à part.
 */
@Component({
  selector: 'app-calendrier',
  imports: [CommonModule],
  templateUrl: './calendrier.component.html',
  styleUrl: './calendrier.component.scss',
})
export class CalendrierComponent implements OnInit {
  items: PlanningItem[] = [];
  loading = true;
  error: string | null = null;

  constructor(private calendrierService: CalendrierService) {}

  get parMois(): MoisGroupe[] {
    const map = new Map<string, PlanningItem[]>();
    for (const item of this.items.filter((i) => i.startDate)) {
      const key = item.startDate!.substring(0, 7); // AAAA-MM
      map.set(key, [...(map.get(key) ?? []), item]);
    }
    return Array.from(map.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([key, items]) => ({
        label: new Date(key + '-15T12:00:00').toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' }),
        items,
      }));
  }

  get coursAlUnite(): PlanningItem[] {
    return this.items.filter((i) => !i.startDate);
  }

  ngOnInit(): void {
    this.calendrierService.getPlanning().subscribe({
      next: (data) => {
        this.items = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiErrorMessage(err, 'Impossible de charger le planning.');
        this.loading = false;
      },
    });
  }
}
