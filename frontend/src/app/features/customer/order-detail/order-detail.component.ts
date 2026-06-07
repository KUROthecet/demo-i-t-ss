import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/services/order-api.service';
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
    private readonly route:    ActivatedRoute,
    private readonly orderApi: OrderApiService,
    private readonly router:   Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderApi.getOrderById(id).subscribe({
      next:  this.onOrderLoaded.bind(this),
      error: this.onOrderLoadError.bind(this)
    });
  }

  private onOrderLoaded(o: Order): void {
    this.order   = o;
    this.loading = false;
  }

  private onOrderLoadError(): void {
    this.error   = 'Order not found.';
    this.loading = false;
  }

  protected cancelOrder(): void {
    if (!this.order) return;
    this.cancelling = true;
    this.orderApi.cancelOrder(this.order.id).subscribe({
      next:  this.onCancelSuccess.bind(this),
      error: this.onCancelError.bind(this)
    });
  }

  private onCancelSuccess(o: Order): void {
    this.order      = o;
    this.cancelling = false;
  }

  private onCancelError(): void {
    this.error      = 'Failed to cancel order.';
    this.cancelling = false;
  }

  protected canCancel(): boolean {
    return this.order?.status === 'PENDING_PROCESSING';
  }

  protected canContinuePayment(): boolean {
    if (!this.order) return false;
    return this.order.status === 'PENDING_PROCESSING' && this.order.paymentStatus === 'PENDING';
  }

  protected continuePayment(): void {
    if (!this.order) return;
    this.router.navigate(['/payment'], { state: { orderData: this.order } });
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
