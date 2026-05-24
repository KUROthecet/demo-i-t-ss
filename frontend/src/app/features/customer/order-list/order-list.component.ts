import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { Order } from '../../../core/models/order.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent, FooterComponent, AmbientBackgroundComponent, VndCurrencyPipe],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.scss'
})
export class OrderListComponent implements OnInit {
  protected email    = '';
  protected orders: Order[] = [];
  protected loading  = false;
  protected searched = false;
  protected error    = '';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {}

  protected trackOrders(): void {
    if (!this.email.trim()) return;
    this.loading  = true;
    this.searched = false;
    this.api.getOrdersByEmail(this.email.trim()).subscribe({
      next:  (orders) => { this.orders = orders; this.searched = true; this.loading = false; },
      error: ()       => { this.error = 'Failed to retrieve orders.'; this.loading = false; }
    });
  }

  protected getStatusBadge(status: string): string {
    switch (status) {
      case 'PENDING_PROCESSING': return 'badge--warning';
      case 'APPROVED':           return 'badge--success';
      case 'REJECTED':           return 'badge--error';
      case 'CANCELLED':          return 'badge--muted';
      default:                   return 'badge--muted';
    }
  }

  protected formatDate(d: string): string {
    return new Date(d).toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
