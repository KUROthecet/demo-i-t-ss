import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/user.model';
import { environment } from '../../../environments/environment';

const TOKEN_KEY = 'aims_token';
const USER_KEY  = 'aims_user';
const CART_KEY  = 'aims_cart';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = environment.apiUrl;

  private readonly _currentUser = signal<LoginResponse | null>(this.loadUserFromStorage());

  readonly currentUser = this._currentUser.asReadonly();
  readonly isLoggedIn  = computed(() => !!this._currentUser());
  readonly isAdmin     = computed(() => this.hasRole('ADMIN'));
  readonly isManager   = computed(() => this.hasRole('PRODUCT_MANAGER') || this.hasRole('ADMIN'));

  private hasRole(role: 'ADMIN' | 'PRODUCT_MANAGER'): boolean {
    return this._currentUser()?.roles?.includes(role) ?? false;
  }

  constructor(
    private readonly http:   HttpClient,
    private readonly router: Router
  ) {}

  changePassword(currentPassword: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/auth/change-password`, {
      currentPassword,
      newPassword
    });
  }

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, data).pipe(
      tap(this.saveSession.bind(this))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(CART_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/home']).then(this.reloadPage.bind(this));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getCurrentUser(): LoginResponse | null {
    return this._currentUser();
  }

  private reloadPage(): void {
    window.location.reload();
  }

  private saveSession(data: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(data));
    this._currentUser.set(data);
  }

  private loadUserFromStorage(): LoginResponse | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
