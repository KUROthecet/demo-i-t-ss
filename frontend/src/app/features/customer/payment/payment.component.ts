import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/services/order-api.service';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { loadScript } from '@paypal/paypal-js';
import { lastValueFrom } from 'rxjs';
import { PAYPAL_CLIENT_ID, VIETQR } from '../../../core/config/app.constants';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, VndCurrencyPipe],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit {
  protected order: any | null = null;
  protected paymentMethod     = '';
  protected paypalCapturing   = false;
  protected paypalDone        = false;
  protected paypalError       = '';
  protected loading           = false;

  constructor(
    private readonly router:   Router,
    private readonly route:    ActivatedRoute,
    private readonly orderApi: OrderApiService
  ) {
    const nav   = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as { orderData: any } | undefined;
    if (state?.orderData) {
      this.order         = typeof state.orderData === 'string'
        ? JSON.parse(state.orderData)
        : state.orderData;
      this.paymentMethod = this.order.paymentMethod;
    }
  }

  async ngOnInit(): Promise<void> {
    if (!this.order) {
      const orderId = Number(this.route.snapshot.paramMap.get('orderId'));
      if (!orderId) {
        this.router.navigate(['/home']);
        return;
      }
      this.loading = true;
      try {
        this.order         = await lastValueFrom(this.orderApi.getOrderById(orderId));
        this.paymentMethod = this.order.paymentMethod;
      } catch {
        this.router.navigate(['/home']);
        return;
      } finally {
        this.loading = false;
      }
    }

    if (
      this.paymentMethod === 'PAYPAL' &&
      this.order.paymentStatus === 'PENDING' &&
      this.order.paymentTransactionId
    ) {
      await this.initPaypalContinueButton();
    }
  }

  private async onPaypalContinueCreateOrder(_data: any, _actions: any): Promise<string> {
    return this.order.paymentTransactionId;
  }

  private async onPaypalContinueApprove(data: any, _actions: any): Promise<void> {
    this.paypalCapturing = true;
    this.paypalError     = '';
    try {
      await lastValueFrom(this.orderApi.captureOrder(data.orderID));
      this.order      = { ...this.order, paymentStatus: 'PAID' };
      this.paypalDone = true;
    } catch {
      this.paypalError = 'Payment capture failed. Please contact support.';
    } finally {
      this.paypalCapturing = false;
    }
  }

  private onPaypalContinueError(_err: any): void {
    if (!this.paypalError) this.paypalError = 'An error occurred during the PayPal transaction.';
  }

  private async initPaypalContinueButton(): Promise<void> {
    try {
      const paypal = await loadScript({ clientId: PAYPAL_CLIENT_ID, currency: 'USD', locale: 'en_US' });
      if (!paypal?.Buttons) return;

      paypal.Buttons({
        createOrder: this.onPaypalContinueCreateOrder.bind(this),
        onApprove:   this.onPaypalContinueApprove.bind(this),
        onError:     this.onPaypalContinueError.bind(this)
      }).render('#paypal-resume-container');
    } catch {
      this.paypalError = 'Could not load payment gateway. Please try again.';
    }
  }

  get isPaid(): boolean {
    return this.order?.paymentStatus === 'PAID' || this.paypalDone;
  }

  get vietQrLink(): string {
    if (!this.order) return '';
    const memo = 'DH' + this.order.id;
    return `https://img.vietqr.io/image/${VIETQR.bankId}-${VIETQR.accountNo}-qr_only.png?amount=${this.order.totalAmount}&addInfo=${memo}&accountName=${VIETQR.accountName}`;
  }
}
