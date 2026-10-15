import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuditLog } from '../../../../shared/models/audit-log.model';
import { apiErrorMessage } from '../../../../shared/utils/api-error';
import { AuditService } from '../../services/audit.service';

/**
 * Consultation du journal d'audit stocké dans MongoDB (ADMIN).
 * [Benjamin – #07, 2026-10-15] Création.
 */
@Component({
  selector: 'app-audit-list',
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-list.component.html',
})
export class AuditListComponent implements OnInit {
  readonly actions = ['LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGIN_BLOCKED', 'CREATE', 'UPDATE', 'DELETE'];
  logs: AuditLog[] = [];
  username = '';
  action = '';
  page = 0;
  readonly size = 20;
  total = 0;
  loading = false;
  error: string | null = null;

  constructor(private auditService: AuditService) {}

  get lastPage(): number {
    return Math.max(0, Math.ceil(this.total / this.size) - 1);
  }

  ngOnInit(): void {
    this.search();
  }

  search(page = 0): void {
    this.page = page;
    this.loading = true;
    this.error = null;
    this.auditService.search(this.username, this.action, page, this.size).subscribe({
      next: (res) => {
        this.logs = res.content;
        this.total = res.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = apiErrorMessage(err, 'Journal indisponible (MongoDB arrêté ?).');
        this.loading = false;
      },
    });
  }
}
