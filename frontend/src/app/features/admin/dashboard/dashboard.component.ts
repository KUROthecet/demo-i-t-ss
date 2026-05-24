import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  totalUsers   = 0;
  activeUsers  = 0;
  blockedUsers = 0;
  totalOrders  = 0;
  pendingOrders = 0;
  recentUsers: any[] = [];
  loading = true;
  mobileMenuOpen = false;
  user = this.auth.getCurrentUser();
  readonly skeletons = Array(5).fill(0);

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit(): void {
    Promise.all([
      this.api.getUsers().toPromise(),
      this.api.getOrders().toPromise(),
      this.api.getPendingOrders().toPromise()
    ]).then(([users, orders, pending]) => {
      this.totalUsers    = users?.length || 0;
      this.activeUsers   = users?.filter(u => u.status === 'ACTIVE').length || 0;
      this.blockedUsers  = users?.filter(u => u.status === 'BLOCKED').length || 0;
      this.recentUsers   = (users || []).slice(0, 8);
      this.totalOrders   = orders?.length || 0;
      this.pendingOrders = pending?.length || 0;
      this.loading = false;
    }).catch(() => { this.loading = false; });
  }

  onMouseMove(event: MouseEvent): void {
    const el = event.currentTarget as HTMLElement;
    const rect = el.getBoundingClientRect();
    el.style.setProperty('--mx', `${event.clientX - rect.left}px`);
    el.style.setProperty('--my', `${event.clientY - rect.top}px`);
  }
}
