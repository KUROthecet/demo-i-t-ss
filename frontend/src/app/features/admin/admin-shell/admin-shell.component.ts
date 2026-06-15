import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { NotificationBellComponent } from '../../../shared/notification-bell/notification-bell.component';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, AmbientBackgroundComponent, NotificationBellComponent],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.scss'
})
export class AdminShellComponent implements OnInit {
  sidebarCollapsed = false;
  mobileOpen       = false;
  currentUrl       = '';
  user: any        = null;

  showChangePw = false;
  cpCurrentPw  = '';
  cpNewPw      = '';
  cpError      = '';
  cpSuccess    = false;
  cpLoading    = false;

  navItems = [
    { label: 'Dashboard',       path: '/admin/dashboard', exact: true,  icon: 'dashboard' },
    { label: 'User Management', path: '/admin/users',     exact: false, icon: 'users'     },
  ];

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    this.user = this.auth.getCurrentUser();
    this.currentUrl = this.router.url;
    this.router.events
      .pipe(filter(this.isNavigationEnd.bind(this)))
      .subscribe(this.onNavigationEnd.bind(this));
  }

  private isNavigationEnd(e: any): boolean {
    return e instanceof NavigationEnd;
  }

  private onNavigationEnd(e: any): void {
    this.currentUrl = e.urlAfterRedirects || e.url;
    this.mobileOpen = false;
  }

  isActive(item: any): boolean {
    if (item.exact) return this.currentUrl === item.path;
    return this.currentUrl.startsWith(item.path);
  }

  openChangePw(): void {
    this.cpCurrentPw  = '';
    this.cpNewPw      = '';
    this.cpError      = '';
    this.cpSuccess    = false;
    this.showChangePw = true;
  }

  closeChangePw(): void {
    this.showChangePw = false;
  }

  submitChangePw(): void {
    if (!this.cpCurrentPw || !this.cpNewPw) {
      this.cpError = 'Both fields are required.';
      return;
    }
    if (this.cpNewPw.length < 6) {
      this.cpError = 'New password must be at least 6 characters.';
      return;
    }
    this.cpLoading = true;
    this.cpError   = '';
    this.auth.changePassword(this.cpCurrentPw, this.cpNewPw).subscribe({
      next: () => {
        this.cpSuccess = true;
        this.cpLoading = false;
        setTimeout(() => this.closeChangePw(), 1500);
      },
      error: (err: any) => {
        this.cpError   = err.error?.message ?? 'Failed to change password.';
        this.cpLoading = false;
      }
    });
  }

  logout() {
    this.auth.logout();
  }

  getUserInitial(): string {
    return (this.user?.fullName || this.user?.username || 'A').charAt(0).toUpperCase();
  }
}
