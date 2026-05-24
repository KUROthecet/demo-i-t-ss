import { Component, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { CartService } from '../../../core/services/cart.service';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

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
  protected deliveryFee = 0;
  protected rushFee     = 0;
  protected calculating = false;
  protected submitting  = false;
  protected error       = '';

  protected items    = computed(() => this.cartService.items());
  protected subtotal = computed(() => this.cartService.subtotal());
  protected vat      = computed(() => this.cartService.vat());
  protected total    = computed(() => this.subtotal() + this.vat() + this.deliveryFee + this.rushFee);

  readonly provinces = [
    'Hanoi', 'Ho Chi Minh City', 'Da Nang', 'Can Tho', 'Hai Phong',
    'Bien Hoa', 'Hue', 'Nha Trang', 'Vung Tau', 'Quy Nhon'
  ];

  constructor(
    private readonly api: ApiService,
    private readonly cartService: CartService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    if (this.cartService.itemCount() === 0) this.router.navigate(['/cart']);
  }

  protected calculateShipping(): void {
    if (!this.province) return;
    const weight = this.items().reduce((sum, i) => sum + i.weight * i.cartQty, 0);
    this.calculating = true;
    this.api.calculateShipping({
      weight, province: this.province,
      orderTotal: this.subtotal(), rushDelivery: this.rushDelivery
    }).subscribe({
      next: (res) => { this.deliveryFee = res.deliveryFee; this.rushFee = res.rushFee; this.calculating = false; },
      error: ()   => { this.calculating = false; }
    });
  }

  protected canRush(): boolean { return this.items().every(i => i.supportRushDelivery); }

  protected placeOrder(): void {
    if (!this.customerName || !this.customerEmail || !this.customerPhone || !this.deliveryAddress || !this.province) {
      this.error = 'Please fill in all required fields.';
      return;
    }
    const nameRegex = /^[\p{L}\s]+$/u;
    if (!nameRegex.test(this.customerName)) {
      this.error = 'Customer name must contain only letters and spaces.';
      return;
    }
    const phoneRegex = /^\d+$/;
    if (!phoneRegex.test(this.customerPhone)) {
      this.error = 'Customer phone must contain only digits.';
      return;
    }
    if (this.rushDelivery && !this.canRush()) {
      this.error = 'Not all items support rush delivery.';
      return;
    }
    this.submitting = true;
    this.error = '';
    const orderReq = {
      customerName:          this.customerName,
      customerEmail:         this.customerEmail,
      customerPhone:         this.customerPhone,
      deliveryAddress:       this.deliveryAddress,
      province:              this.province,
      deliveryNotes:         this.deliveryNotes,
      rushDelivery:          this.rushDelivery,
      preferredDeliveryTime: this.preferredTime,
      paymentMethod:         this.paymentMethod,
      orderLines:            this.items().map(i => ({ mediaId: i.id, quantity: i.cartQty }))
    };
    this.api.placeOrder(orderReq).subscribe({
      next: (order) => {
        this.cartService.clearCart();
        this.router.navigate(['/payment'], { state: { order, paymentMethod: this.paymentMethod } });
      },
      error: (err) => {
        this.error      = err.error?.message ?? 'Failed to place order. Please try again.';
        this.submitting = false;
      }
    });
  }
}
