/*
Coupling level: Common Coupling
Reason why: Multiple components read/write directly to the browser's global localStorage.
*/

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterModule, AmbientBackgroundComponent],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.scss'
})
export class AdminShellComponent implements OnInit {
  sidebarCollapsed = false;
  mobileOpen = false;
  currentUrl = '';
  user: any = null;

  navItems = [
    { label: 'Dashboard',       path: '/admin/dashboard', exact: true,  icon: 'dashboard' },
    { label: 'User Management', path: '/admin/users',     exact: false, icon: 'users'     },
  ];

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit() {
    this.user = this.auth.getCurrentUser();
    this.currentUrl = this.router.url;
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => {
        this.currentUrl = e.urlAfterRedirects || e.url;
        this.mobileOpen = false;
      });
  }

  isActive(item: any): boolean {
    if (item.exact) return this.currentUrl === item.path;
    return this.currentUrl.startsWith(item.path);
  }

  logout() {
    this.auth.logout();
  }

  getUserInitial(): string {
    return (this.user?.fullName || this.user?.username || 'A').charAt(0).toUpperCase();
  }
}
