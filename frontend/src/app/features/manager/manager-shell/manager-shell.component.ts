import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';

@Component({
  selector: 'app-manager-shell',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, AmbientBackgroundComponent],
  templateUrl: './manager-shell.component.html',
  styleUrl: './manager-shell.component.scss'
})
export class ManagerShellComponent implements OnInit {
  sidebarCollapsed = false;
  mobileOpen = false;
  currentUrl = '';
  user: any = null;

  navItems = [
    { label: 'Dashboard',      path: '/manager/dashboard',     exact: true,  icon: 'dashboard' },
    { label: 'All Products',   path: '/manager/products',      exact: false, icon: 'products'  },
    { label: 'Add Product',    path: '/manager/product-form',  exact: false, icon: 'add'       },
    { label: 'Stock History',  path: '/manager/stock-history', exact: false, icon: 'stock'     },
    { label: 'Product History',path: '/manager/history',       exact: false, icon: 'history'   },
    { label: 'Process Orders', path: '/manager/orders',        exact: false, icon: 'orders'    },
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
    return (this.user?.fullName || this.user?.username || 'M').charAt(0).toUpperCase();
  }
}
