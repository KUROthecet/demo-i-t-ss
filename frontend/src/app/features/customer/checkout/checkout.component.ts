import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderApiService } from '../../../core/services/order-api.service';
import { CartService } from '../../../core/services/cart.service';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { loadScript } from '@paypal/paypal-js';
import { lastValueFrom } from 'rxjs';
import { PAYPAL_CLIENT_ID } from '../../../core/config/app.constants';
import { AppConfigService } from '../../../core/services/app-config.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, FooterComponent, AmbientBackgroundComponent, VndCurrencyPipe],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss'
})
export class CheckoutComponent implements OnInit {
  protected customerName    = '';
  protected customerEmail   = '';
  protected customerPhone   = '';
  protected deliveryAddress = '';
  protected province        = '';
  protected deliveryNotes   = '';
  protected rushDelivery    = false;
  protected preferredTime   = '';
  protected paymentMethod: 'VIETQR' | 'PAYPAL' = 'VIETQR';
  protected deliveryFee     = 0;
  protected rushFee         = 0;
  protected calculating     = false;
  protected submitting        = false;
  protected error             = '';
  private   placedOrder: any  = null;
  private   pendingPaypalId   = '';


  get freeShippingThreshold(): number { return this.appConfig.freeShippingThreshold; }
  get freeShippingCap(): number { return this.appConfig.freeShippingCap; }

  protected items    = computed(this.computeItems.bind(this));
  protected subtotal = computed(this.computeSubtotal.bind(this));
  protected vat      = computed(this.computeVat.bind(this));
  protected total    = computed(this.computeTotal.bind(this));

  readonly provinces = [
    'Hanoi', 'Ho Chi Minh City', 'Da Nang', 'Can Tho', 'Hai Phong',
    'Bien Hoa', 'Hue', 'Nha Trang', 'Vung Tau', 'Quy Nhon'
  ];

  constructor(
    private readonly orderApi:    OrderApiService,
    private readonly cartService: CartService,
    private readonly router:      Router,
    private readonly appConfig:   AppConfigService
  ) {}

  async ngOnInit() {
    if (this.cartService.itemCount() === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    this.cartService.refreshStock().subscribe();

    try {
      const paypal = await loadScript({
        clientId: PAYPAL_CLIENT_ID,
        currency: 'USD',
        locale:   'en_US'
      });

      if (paypal && paypal.Buttons) {
        paypal.Buttons({
          createOrder: this.onPaypalCreateOrder.bind(this),
          onApprove:   this.onPaypalApprove.bind(this),
          onCancel:    this.onPaypalCancel.bind(this),
          onError:     this.onPaypalError.bind(this)
        }).render('#paypal-button-container');
      }
    } catch {
      this.error = 'Failed to load payment gateway.';
    }
  }

  private async onPaypalCreateOrder(_data: any, _actions: any): Promise<string> {
    if (!this.validateForm()) throw new Error('Form validation failed');

    if (this.pendingPaypalId) {
      return this.pendingPaypalId;
    }

    if (this.submitting) throw new Error('Order already in progress.');
    this.submitting = true;
    this.error      = '';

    try {
      const rawResponse = await lastValueFrom(this.orderApi.placeOrder(this.buildOrderRequest()));
      const savedOrder  = typeof rawResponse === 'string' ? JSON.parse(rawResponse) : rawResponse;
      this.placedOrder  = savedOrder;

      const paypalOrderId = savedOrder.paymentTransactionId;
      if (!paypalOrderId) throw new Error('PayPal Order ID not found in server response.');
      this.pendingPaypalId = paypalOrderId;
      return paypalOrderId;
    } catch (err: any) {
      this.submitting = false;
      this.error = err.error?.message ?? err.message ?? 'Failed to place order. Please try again.';
      throw err;
    }
  }

  private async onPaypalApprove(data: any, _actions: any): Promise<void> {
    try {
      await lastValueFrom(this.orderApi.captureOrder(data.orderID));
      this.pendingPaypalId = '';
      this.cartService.clearCart();
      this.router.navigate(['/payment', this.placedOrder.id], { state: { orderData: this.placedOrder } });
    } catch {
      this.error      = 'Payment capture failed. Please contact support.';
      this.submitting = false;
    }
  }

  private onPaypalCancel(_data: any): void {
    this.submitting = false;
  }

  private onPaypalError(_err: any): void {
    if (!this.error) this.error = 'An error occurred during the PayPal transaction.';
    this.submitting = false;
  }

  private computeItems(): any[] {
    return this.cartService.items();
  }

  private computeSubtotal(): number {
    return this.cartService.subtotal();
  }

  private computeVat(): number {
    return this.cartService.vat();
  }

  private computeTotal(): number {
    return this.subtotal() + this.vat() + this.deliveryFee + this.rushFee;
  }

  protected isRushEligibleProvince(): boolean {
    return this.appConfig.rushEligibleProvinces.includes(this.province);
  }

  private validateForm(): boolean {
    if (!this.customerName || !this.customerEmail || !this.customerPhone || !this.deliveryAddress || !this.province) {
      this.error = 'Please fill in all required fields.';
      return false;
    }
    if (!/^[\p{L}\s]+$/u.test(this.customerName)) {
      this.error = 'Customer name must contain only letters and spaces.';
      return false;
    }
    if (!/^\d+$/.test(this.customerPhone)) {
      this.error = 'Customer phone must contain only digits.';
      return false;
    }
    if (this.rushDelivery && !this.isRushEligibleProvince()) {
      this.error = 'Rush delivery is only available in Hanoi and Ho Chi Minh City.';
      return false;
    }
    if (this.rushDelivery && !this.canRush()) {
      this.error = 'Not all items support rush delivery.';
      return false;
    }
    return true;
  }

  private calculateTotalWeight(): number {
    let weight = 0;
    for (const item of this.items()) {
      weight += item.weight * item.cartQty;
    }
    return weight;
  }

  private buildOrderLine(item: any): { mediaId: number; quantity: number } {
    return { mediaId: item.id, quantity: item.cartQty };
  }

  private buildOrderLines(): Array<{ mediaId: number; quantity: number }> {
    const lines: Array<{ mediaId: number; quantity: number }> = [];
    for (const item of this.items()) {
      lines.push(this.buildOrderLine(item));
    }
    return lines;
  }

  private buildOrderRequest(): object {
    return {
      customerName:          this.customerName,
      customerEmail:         this.customerEmail,
      customerPhone:         this.customerPhone,
      deliveryAddress:       this.deliveryAddress,
      province:              this.province,
      deliveryNotes:         this.deliveryNotes,
      rushDelivery:          this.rushDelivery,
      preferredDeliveryTime: this.preferredTime,
      paymentMethod:         this.paymentMethod,
      orderLines:            this.buildOrderLines()
    };
  }

  private onShippingCalculated(res: any): void {
    this.deliveryFee = res.deliveryFee;
    this.rushFee     = res.rushFee;
    this.calculating = false;
  }

  private onShippingError(): void {
    this.calculating = false;
  }

  protected calculateShipping(): void {
    if (!this.province) return;
    if (this.rushDelivery && !this.isRushEligibleProvince()) {
      this.rushDelivery = false;
    }
    this.calculating = true;
    this.orderApi.calculateShipping({
      weight:       this.calculateTotalWeight(),
      province:     this.province,
      orderTotal:   this.subtotal(),
      rushDelivery: this.rushDelivery
    }).subscribe({
      next:  this.onShippingCalculated.bind(this),
      error: this.onShippingError.bind(this)
    });
  }

  private onOrderPlaced(rawResponse: any): void {
    this.cartService.clearCart();
    const order = typeof rawResponse === 'string' ? JSON.parse(rawResponse) : rawResponse;
    this.router.navigate(['/payment', order.id], { state: { orderData: order } });
  }

  private onOrderError(err: any): void {
    this.error      = err.error?.message ?? 'Failed to place order. Please try again.';
    this.submitting = false;
  }

  protected canRush(): boolean {
    for (const i of this.items()) {
      if (!i.supportRushDelivery) return false;
    }
    return true;
  }

  protected placeOrder(): void {
    if (!this.validateForm()) return;
    this.submitting = true;
    this.error      = '';
    this.orderApi.placeOrder(this.buildOrderRequest()).subscribe({
      next:  this.onOrderPlaced.bind(this),
      error: this.onOrderError.bind(this)
    });
  }
}
