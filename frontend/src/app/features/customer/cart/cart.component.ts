import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { CartService } from '../../../core/services/cart.service';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, FooterComponent, AmbientBackgroundComponent, VndCurrencyPipe],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent {
  protected items    = computed(() => this.cartService.items());
  protected count    = computed(() => this.cartService.itemCount());
  protected subtotal = computed(() => this.cartService.subtotal());
  protected vat      = computed(() => this.cartService.vat());
  protected total    = computed(() => this.cartService.total());
  protected hasStockError = computed(() => this.items().some(i => i.cartQty > i.quantityInStock));

  constructor(private readonly cartService: CartService, private readonly router: Router) {}

  protected updateQty(id: number, qty: number): void { this.cartService.updateQuantity(id, qty); }
  protected remove(id: number): void { this.cartService.removeItem(id); }
  protected clear(): void { this.cartService.clearCart(); }
  protected checkout(): void { this.router.navigate(['/checkout']); }
}
