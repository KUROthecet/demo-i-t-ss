import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
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
  username = 'admin';
  password = 'admin123';
  showPassword = false;
  loading = false;
  error = '';

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  selectRole(role: 'admin' | 'pm'): void {
    this.selectedRole = role;
    this.username = role === 'admin' ? 'admin' : 'manager';
    this.password = role === 'admin' ? 'admin123' : 'manager123';
  }

  login(): void {
    if (!this.username || !this.password) {
      this.error = 'Please enter your username and password.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.api.login({ username: this.username, password: this.password }).subscribe({
      next: (res) => {
        this.auth.saveSession(res);
        if (res.role === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/manager/dashboard']);
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Invalid credentials or account blocked.';
        this.loading = false;
      }
    });
  }
}
