import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/services/order-api.service';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { loadScript } from '@paypal/paypal-js';
import { lastValueFrom } from 'rxjs';

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

  private readonly PAYPAL_CLIENT_ID =
    'AS80_ZkaQeM3a3jW8ymmla5sNV-j0j5wiyh2nvRfhtFn5x1dFZ26UgpWL7yB6eJMW-FUc1-3LQr3LRVB';

  constructor(
    private readonly router:   Router,
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
      this.router.navigate(['/home']);
      return;
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
      const paypal = await loadScript({ clientId: this.PAYPAL_CLIENT_ID, currency: 'USD' });
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
    const bankId      = 'MB';
    const accountNo   = '0975452106';
    const accountName = 'BUI TRUNG HIEU';
    const memo        = 'DH' + this.order.id;
    return `https://img.vietqr.io/image/${bankId}-${accountNo}-qr_only.png?amount=${this.order.totalAmount}&addInfo=${memo}&accountName=${accountName}`;
  }
}
