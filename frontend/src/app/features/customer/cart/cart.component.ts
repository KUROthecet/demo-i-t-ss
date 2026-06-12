import { Component, OnInit, computed } from '@angular/core';
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
export class CartComponent implements OnInit {
  protected items         = computed(this.computeItems.bind(this));
  protected count         = computed(this.computeCount.bind(this));
  protected subtotal      = computed(this.computeSubtotal.bind(this));
  protected vat           = computed(this.computeVat.bind(this));
  protected total         = computed(this.computeTotal.bind(this));
  protected hasStockError = computed(this.computeHasStockError.bind(this));

  constructor(private readonly cartService: CartService, private readonly router: Router) {}

  ngOnInit(): void {
    this.cartService.refreshStock().subscribe();
  }

  protected updateQty(id: number, qty: number): void { this.cartService.updateQuantity(id, qty); }
  protected remove(id: number): void { this.cartService.removeItem(id); }
  protected clear(): void { this.cartService.clearCart(); }
  protected checkout(): void { this.router.navigate(['/checkout']); }

  private computeItems(): any[] {
    return this.cartService.items();
  }

  private computeCount(): number {
    return this.cartService.itemCount();
  }

  private computeSubtotal(): number {
    return this.cartService.subtotal();
  }

  private computeVat(): number {
    return this.cartService.vat();
  }

  private computeTotal(): number {
    return this.cartService.total();
  }

  private computeHasStockError(): boolean {
    for (const item of this.cartService.items()) {
      if (item.cartQty > item.quantityInStock) return true;
    }
    return false;
  }
}
