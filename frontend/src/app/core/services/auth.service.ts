/*
Coupling level: Common Coupling
Reason why: Multiple components read/write directly to the browser's global localStorage.
*/

import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { LoginResponse } from '../models/user.model';

const TOKEN_KEY = 'aims_token';
const USER_KEY  = 'aims_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private router: Router) {}

  saveSession(data: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(data));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getCurrentUser(): LoginResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return this.getCurrentUser()?.role === 'ADMIN';
  }

  isManager(): boolean {
    const role = this.getCurrentUser()?.role;
    return role === 'PRODUCT_MANAGER' || role === 'ADMIN';
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.router.navigate(['/login']);
  }
}
