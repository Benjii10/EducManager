import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  // [Benjamin – #01, 2026-10-15] login asynchrone via l'API (avant : comparaison locale synchrone)
  onSubmit(): void {
    this.error = '';
    this.loading = true;
    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate([this.authService.getDefaultRoute()]);
      },
      error: (err) => {
        this.loading = false;
        this.error =
          err.status === 401
            ? 'Email ou mot de passe incorrect.'
            : 'Serveur injoignable. Vérifiez que le backend est démarré (port 8080).';
      },
    });
  }
}
