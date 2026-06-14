import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MediaApiService } from '../../../core/services/media-api.service';
import { OrderApiService } from '../../../core/services/order-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppConstants } from '../../../core/config/app.constants';

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
  historyLogs: any[]                  = [];
  catalogStats: Record<string, number> = {};
  loading        = true;
  user           = this.auth.getCurrentUser();
  readonly skeletons = Array(5).fill(0);
  protected readonly maxDailyDelete         = AppConstants.MAX_DAILY_DELETE;
  protected readonly deleteWarningThreshold = AppConstants.DAILY_DELETE_WARNING_THRESHOLD;

  private readonly C = 239;

  private static readonly CATALOG_COLORS: Record<string, string> = {
    Book: '#60a5fa', CD: '#fb923c', DVD: '#c084fc', Newspaper: '#1DB954'
  };

  constructor(
    private readonly mediaApi: MediaApiService,
    private readonly orderApi: OrderApiService,
    private readonly auth:     AuthService
  ) {}

  ngOnInit(): void {
    this.mediaApi.getDailyDeleteCount().subscribe(this.onDailyDeleteCountLoaded.bind(this));
    this.mediaApi.getHistoryLogs().subscribe(this.onHistoryLogsLoaded.bind(this));
    this.mediaApi.getProducts().subscribe(this.onProductsLoaded.bind(this));
    this.mediaApi.getCatalogStats().subscribe(this.onCatalogStatsLoaded.bind(this));
    this.orderApi.getPendingOrders().subscribe(this.onPendingOrdersLoaded.bind(this));
    this.orderApi.getOrders().subscribe(this.onAllOrdersLoaded.bind(this));
  }

  private onDailyDeleteCountLoaded(res: any): void {
    this.dailyDeleteUsed = res?.count || 0;
  }

  private onHistoryLogsLoaded(res: any): void {
    this.historyLogs = (res || []).slice(0, 8);
  }

  private onProductsLoaded(res: any): void {
    this.totalProducts = res?.length || 0;
  }

  private onCatalogStatsLoaded(res: any): void {
    this.catalogStats = res || {};
  }

  private onPendingOrdersLoaded(res: any): void {
    this.pendingCount = res?.totalElements || 0;
  }

  private onAllOrdersLoaded(res: any): void {
    this.totalOrders = res?.totalElements || 0;
    this.loading = false;
  }

  get processedOrders(): number {
    return Math.max(0, this.totalOrders - this.pendingCount);
  }

  get processedDash(): string {
    if (this.totalOrders === 0) return `1 ${this.C}`;
    return `${(this.processedOrders / this.totalOrders) * this.C} ${this.C}`;
  }

  get pendingDash(): string {
    if (this.totalOrders === 0) return `0 ${this.C}`;
    return `${(this.pendingCount / this.totalOrders) * this.C} ${this.C}`;
  }

  get pendingRotation(): string {
    if (this.totalOrders === 0) return 'rotate(-90 50 50)';
    return `rotate(${-90 + (this.processedOrders / this.totalOrders) * 360} 50 50)`;
  }

  private getMaxCatalogCount(): number {
    let max = 1;
    for (const count of Object.values(this.catalogStats)) {
      if (count > max) max = count;
    }
    return max;
  }

  private buildCatalogItem(label: string, max: number): { label: string; count: number; color: string; pct: number } {
    const count = this.catalogStats[label] || 0;
    const color = ManagerDashboardComponent.CATALOG_COLORS[label] ?? '#888';
    return { label, count, color, pct: Math.round((count / max) * 100) };
  }

  get catalogItems(): { label: string; count: number; color: string; pct: number }[] {
    const max    = this.getMaxCatalogCount();
    const result: { label: string; count: number; color: string; pct: number }[] = [];
    for (const label of Object.keys(this.catalogStats)) {
      result.push(this.buildCatalogItem(label, max));
    }
    return result;
  }

  formatDate(d: string): string {
    if (!d) return '';
    return new Date(d).toLocaleString('vi-VN');
  }

  getActionColor(actionType: string): string {
    if (actionType?.includes('ADD'))     return '#1DB954';
    if (actionType?.includes('UPDATE'))  return '#60a5fa';
    if (actionType?.includes('DELETE'))  return '#f87171';
    if (actionType?.includes('APPROVE')) return '#f59e0b';
    return 'rgba(255,255,255,0.5)';
  }

  getActionBg(actionType: string): string {
    if (actionType?.includes('ADD'))     return 'rgba(29,185,84,0.12)';
    if (actionType?.includes('UPDATE'))  return 'rgba(96,165,250,0.12)';
    if (actionType?.includes('DELETE'))  return 'rgba(239,68,68,0.12)';
    if (actionType?.includes('APPROVE')) return 'rgba(245,158,11,0.12)';
    return 'rgba(255,255,255,0.06)';
  }

  onMouseMove(event: MouseEvent): void {
    const el   = event.currentTarget as HTMLElement;
    const rect = el.getBoundingClientRect();
    el.style.setProperty('--mx', `${event.clientX - rect.left}px`);
    el.style.setProperty('--my', `${event.clientY - rect.top}px`);
  }
}
