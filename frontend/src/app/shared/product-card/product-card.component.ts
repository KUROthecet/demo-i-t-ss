import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Media, isBook, isCD, isDVD, isNewspaper } from '../../core/models/media.model';
import { CartService } from '../../core/services/cart.service';
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

  constructor(private readonly cartService: CartService) {}

  protected onAddToCart(event: MouseEvent): void {
    event.stopPropagation();
    event.preventDefault();
    this.cartService.addToCart(this.product, 1);
    this.added = true;
    setTimeout(() => (this.added = false), 2000);
  }

  protected getImageUrl(): string {
    if (this.product.imageUrl) return this.product.imageUrl;
    const fallbacks: Record<string, string> = {
      Book:      'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80',
      CD:        'https://images.unsplash.com/photo-1619983081563-430f63602796?auto=format&fit=crop&w=600&q=80',
      DVD:       'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80',
      Newspaper: 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=600&q=80',
    };
    return fallbacks[this.product.category] ?? fallbacks['Book'];
  }

  protected getSubtitle(): string {
    if (isBook(this.product))      return this.product.author ?? '';
    if (isCD(this.product))        return this.product.artist ?? '';
    if (isDVD(this.product))       return this.product.director ?? '';
    if (isNewspaper(this.product)) return this.product.editorInChief ? `Ed. ${this.product.editorInChief}` : '';
    return '';
  }
}
