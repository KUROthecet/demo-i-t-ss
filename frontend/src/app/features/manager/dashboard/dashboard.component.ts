import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class ManagerDashboardComponent implements OnInit {
  pendingCount    = 0;
  totalOrders     = 0;
  totalProducts   = 0;
  dailyDeleteUsed = 0;
  historyLogs: any[] = [];
  loading = true;
  mobileMenuOpen = false;
  user = this.auth.getCurrentUser();
  readonly skeletons = Array(5).fill(0);

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit(): void {
    Promise.all([
      this.api.getPendingOrders(),
      this.api.getOrders(),
      this.api.getDailyDeleteCount(),
      this.api.getHistoryLogs(),
      this.api.getProducts()
    ]).then(([pending, all, delCount, logs, products]) => {
      pending.subscribe(res => this.pendingCount = res?.length || 0);
      all.subscribe(res => this.totalOrders = res?.length || 0);
      delCount.subscribe(res => this.dailyDeleteUsed = res?.count || 0);
      logs.subscribe(res => this.historyLogs = (res || []).slice(0, 8));
      products.subscribe(res => this.totalProducts = res?.length || 0);

      this.loading = false;
    }).catch(() => { this.loading = false; });
  }

  formatDate(d: string): string {
    if (!d) return '';
    return new Date(d).toLocaleString('vi-VN');
  }

  getActionColor(actionType: string): string {
    if (actionType?.includes('ADD')) return '#1DB954';
    if (actionType?.includes('UPDATE')) return '#60a5fa';
    if (actionType?.includes('DELETE')) return '#f87171';
    if (actionType?.includes('APPROVE')) return '#f59e0b';
    return 'rgba(255,255,255,0.5)';
  }

  getActionBg(actionType: string): string {
    if (actionType?.includes('ADD')) return 'rgba(29,185,84,0.12)';
    if (actionType?.includes('UPDATE')) return 'rgba(96,165,250,0.12)';
    if (actionType?.includes('DELETE')) return 'rgba(239,68,68,0.12)';
    if (actionType?.includes('APPROVE')) return 'rgba(245,158,11,0.12)';
    return 'rgba(255,255,255,0.06)';
  }

  onMouseMove(event: MouseEvent): void {
    const el = event.currentTarget as HTMLElement;
    const rect = el.getBoundingClientRect();
    el.style.setProperty('--mx', `${event.clientX - rect.left}px`);
    el.style.setProperty('--my', `${event.clientY - rect.top}px`);
  }
}
