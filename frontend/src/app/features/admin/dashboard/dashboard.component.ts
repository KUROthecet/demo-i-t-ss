import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { UserApiService } from '../../../core/services/user-api.service';
import { OrderApiService } from '../../../core/services/order-api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  totalUsers    = 0;
  activeUsers   = 0;
  blockedUsers  = 0;
  totalOrders   = 0;
  pendingOrders = 0;
  recentUsers: any[] = [];
  loading        = true;
  user           = this.auth.getCurrentUser();
  readonly skeletons = Array(5).fill(0);

  private readonly C = 239;

  constructor(
    private readonly userApi:  UserApiService,
    private readonly orderApi: OrderApiService,
    private readonly auth:     AuthService
  ) {}

  ngOnInit(): void {
    Promise.all([
      firstValueFrom(this.userApi.getUsers()),
      firstValueFrom(this.orderApi.getOrders()),
      firstValueFrom(this.orderApi.getPendingOrders())
    ]).then(this.onDashboardDataLoaded.bind(this))
      .catch(this.onDashboardError.bind(this));
  }

  private onDashboardDataLoaded(results: any[]): void {
    const users   = results[0] as any[] | undefined;
    const orders  = results[1] as any;
    const pending = results[2] as any;

    this.totalUsers    = users?.length   || 0;
    this.activeUsers   = this.countUsersByStatus(users, 'ACTIVE');
    this.blockedUsers  = this.countUsersByStatus(users, 'BLOCKED');
    this.recentUsers   = (users || []).slice(0, 8);
    this.totalOrders   = orders?.totalElements  || 0;
    this.pendingOrders = pending?.totalElements || 0;
    this.loading       = false;
  }

  private onDashboardError(): void {
    this.loading = false;
  }

  private countUsersByStatus(users: any[] | undefined, status: string): number {
    if (!users) return 0;
    let count = 0;
    for (const u of users) {
      if (u.status === status) count++;
    }
    return count;
  }

  get activeDash(): string {
    if (this.totalUsers === 0) return `${this.C} ${this.C}`;
    return `${(this.activeUsers / this.totalUsers) * this.C} ${this.C}`;
  }

  get blockedDash(): string {
    if (this.totalUsers === 0) return `0 ${this.C}`;
    return `${(this.blockedUsers / this.totalUsers) * this.C} ${this.C}`;
  }

  get blockedRotation(): string {
    if (this.totalUsers === 0) return 'rotate(-90 50 50)';
    return `rotate(${-90 + (this.activeUsers / this.totalUsers) * 360} 50 50)`;
  }

  get activePercent(): number {
    if (this.totalUsers === 0) return 0;
    return Math.round((this.activeUsers / this.totalUsers) * 100);
  }

  get orderFulfillmentRate(): number {
    if (this.totalOrders === 0) return 0;
    return Math.round(((this.totalOrders - this.pendingOrders) / this.totalOrders) * 100);
  }

  onMouseMove(event: MouseEvent): void {
    const el   = event.currentTarget as HTMLElement;
    const rect = el.getBoundingClientRect();
    el.style.setProperty('--mx', `${event.clientX - rect.left}px`);
    el.style.setProperty('--my', `${event.clientY - rect.top}px`);
  }
}
