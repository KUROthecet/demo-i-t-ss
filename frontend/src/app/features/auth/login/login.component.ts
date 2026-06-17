import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, AmbientBackgroundComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  selectedRole: 'admin' | 'pm' = 'admin';
  username     = '';
  password     = '';
  showPassword = false;
  loading      = false;
  error        = '';

  constructor(
    private readonly auth:   AuthService,
    private readonly router: Router
  ) {}

  selectRole(role: 'admin' | 'pm'): void {
    this.selectedRole = role;
    this.username     = '';
    this.password     = '';
    this.error        = '';
  }

  login(): void {
    if (!this.username || !this.password) {
      this.error = 'Please enter your username and password.';
      return;
    }
    this.loading = true;
    this.error   = '';
    this.auth.login({ username: this.username, password: this.password }).subscribe({
      next:  this.onLoginSuccess.bind(this),
      error: this.onLoginError.bind(this)
    });
  }

  private onLoginSuccess(res: any): void {
    if (res.roles?.includes('ADMIN')) {
      this.router.navigate(['/admin/dashboard']);
    } else {
      this.router.navigate(['/manager/dashboard']);
    }
  }

  private onLoginError(err: any): void {
    this.error   = err.error?.message || 'Invalid credentials or account blocked.';
    this.loading = false;
  }
}
