import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { Media } from '../../core/models/media.model';
import { CartService } from '../../core/services/cart.service';
import { MediaDisplayService } from '../../core/services/media-display.service';
import { VndCurrencyPipe } from '../pipes/vnd-currency.pipe';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterLink, VndCurrencyPipe],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss'
})
export class ProductCardComponent {
  @Input() product!: Media;
  @Input() showAddToCart = true;
  @Output() productClick = new EventEmitter<Media>();

  protected isHovered = false;
  protected added     = false;

  readonly starsArray = [0, 1, 2, 3, 4];

  get reviewCount(): number {
    const id = Number(this.product.id) || 1;
    return (id * 47 + 83) % 500 + 12;
  }

  constructor(
    private readonly cartService:  CartService,
    private readonly router:       Router,
    private readonly mediaDisplay: MediaDisplayService
  ) {}

  protected onViewProduct(event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    this.router.navigate(['/product', this.product.id]);
  }

  protected onAddToCart(event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    const added = this.cartService.addToCart(this.product, 1);
    if (added) {
      this.added = true;
      setTimeout(this.clearAdded.bind(this), 2000);
    }
  }

  private clearAdded(): void {
    this.added = false;
  }

  protected getImageUrl(): string {
    return this.mediaDisplay.getImageUrl(this.product);
  }

  protected getSubtitle(): string {
    return this.mediaDisplay.getSubtitle(this.product);
  }
}
