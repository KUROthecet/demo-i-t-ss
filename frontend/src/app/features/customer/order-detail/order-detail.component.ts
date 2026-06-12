import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/services/order-api.service';
import { Order } from '../../../core/models/order.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { OrderStatusPipe } from '../../../shared/pipes/order-status.pipe';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, FooterComponent, VndCurrencyPipe, OrderStatusPipe],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.scss'
})
export class OrderDetailComponent implements OnInit {
  protected order: Order | null = null;
  protected loading          = true;
  protected error            = '';
  protected cancelling       = false;
  protected cancelConfirming = false;

  constructor(
    private readonly route:    ActivatedRoute,
    private readonly orderApi: OrderApiService,
    private readonly router:   Router
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id') ?? '';
    const obs$ = /^\d+$/.test(param)
      ? this.orderApi.getOrderById(Number(param))
      : this.orderApi.getOrderByCode(param);
    obs$.subscribe({
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

  protected requestCancel(): void {
    this.cancelConfirming = true;
  }

  protected confirmCancel(): void {
    if (!this.order) return;
    this.cancelConfirming = false;
    this.cancelling       = true;
    this.orderApi.cancelOrder(this.order.id).subscribe({
      next:  this.onCancelSuccess.bind(this),
      error: this.onCancelError.bind(this)
    });
  }

  protected abortCancel(): void {
    this.cancelConfirming = false;
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
    this.router.navigate(['/payment', this.order.id], { state: { orderData: this.order } });
  }

  protected formatDate(d: string): string {
    return new Date(d).toLocaleString('vi-VN');
  }
}
