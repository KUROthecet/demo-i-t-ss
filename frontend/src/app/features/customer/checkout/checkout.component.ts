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
import { loadScript } from '@paypal/paypal-js';
import { lastValueFrom } from 'rxjs';

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
  protected paymentMethod: 'VIETQR' | 'PAYPAL' = 'PAYPAL';
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

  async ngOnInit() {
    if (this.cartService.itemCount() === 0) {
      this.router.navigate(['/cart']);
      return;
    }

    // Initialize the PayPal SDK
    try {
      const paypal = await loadScript({
        clientId: 'AS80_ZkaQeM3a3jW8ymmla5sNV-j0j5wiyh2nvRfhtFn5x1dFZ26UgpWL7yB6eJMW-FUc1-3LQr3LRVB', // Put your Sandbox Client ID here
        currency: 'USD'
      });

      if (paypal && paypal.Buttons) {
        paypal.Buttons({
          
          // 1. Triggered when the user clicks the yellow PayPal button
          createOrder: async (data, actions) => {
            // First, ensure they filled out your form
            if (!this.validateForm()) {
              throw new Error('Form validation failed'); // Stops PayPal from opening
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

            try {
              // Call your Spring Boot backend to create the order
              // We use lastValueFrom to await the Observable
              const paypalID = await lastValueFrom(this.api.placeOrder(orderReq));
              
              // CRITICAL: Return the PayPal Order ID to the SDK!
              // (Adjust 'paypalOrderId' to whatever property your backend actually returns)
              return paypalID;

            } catch (err: any) {
              this.error = err.error?.message ?? 'Failed to place order. Please try again.';
              throw err;
            } finally {
              this.submitting = false;
            }
          },

          // 2. Triggered after the user logs into the popup and clicks "Pay"
          onApprove: async (data, actions) => {
            this.submitting = true;
            try {
              // Now call the new Spring Boot capture endpoint we built!
              // You will need to add this method to your ApiService
              await lastValueFrom(this.api.captureOrder(data.orderID));

              // Payment is completely finished! Clear cart and route away.
              this.cartService.clearCart();
              this.router.navigate(['/payment'], { 
                state: { orderId: data.orderID, paymentMethod: 'PAYPAL' } 
              });

            } catch (err) {
              this.error = 'Payment capture failed. Please contact support.';
            } finally {
              this.submitting = false;
            }
          },

          // 3. Handle user closing the popup or network issues
          onError: (err) => {
            console.error('PayPal Routing Error:', err);
            // Don't override standard form validation errors if they just failed validation
            if (!this.error) {
              this.error = 'An error occurred during the PayPal transaction.';
            }
          }

        }).render('#paypal-button-container'); // Renders inside the HTML div we added
      }
    } catch (error) {
      console.error('Failed to load the PayPal JS SDK', error);
      this.error = 'Failed to load payment gateway.';
    }
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
    if (this.rushDelivery && !this.canRush()) {
      this.error = 'Not all items support rush delivery.';
      return false;
    }
    return true;
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
    // 1. Instantly reuse the exact same validation logic!
    if (!this.validateForm()) {
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
