import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { Order } from '../../../core/models/order.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, FooterComponent, VndCurrencyPipe],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
})
export class OrderDetailComponent implements OnInit {
  protected order: Order | null = null;
  protected loading    = true;
  protected error      = '';
  protected cancelling = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly api:   ApiService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getOrderById(id).subscribe({
      next:  (o) => { this.order = o; this.loading = false; },
      error: ()  => { this.error = 'Order not found.'; this.loading = false; }
    });
  }

  protected cancelOrder(): void {
    if (!this.order) return;
    this.cancelling = true;
    this.api.cancelOrder(this.order.id).subscribe({
      next:  (o) => { this.order = o; this.cancelling = false; },
      error: ()  => { this.error = 'Failed to cancel order.'; this.cancelling = false; }
    });
  }

  protected canCancel(): boolean {
    return this.order?.status === 'PENDING_PROCESSING';
  }

  protected getStatusBadge(s: string): string {
    switch (s) {
      case 'PENDING_PROCESSING': return 'badge--warning';
      case 'APPROVED':           return 'badge--success';
      case 'REJECTED':           return 'badge--error';
      case 'CANCELLED':          return 'badge--muted';
      default:                   return 'badge--muted';
    }
  }

  protected formatDate(d: string): string {
    return new Date(d).toLocaleString('vi-VN');
  }
}
