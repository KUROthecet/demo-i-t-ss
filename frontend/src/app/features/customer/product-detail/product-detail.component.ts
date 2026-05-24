import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { CartService } from '../../../core/services/cart.service';
import { Media, isBook, isCD, isDVD, isNewspaper } from '../../../core/models/media.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

/**
 * ProductDetailComponent — premium product detail page.
 * Features: 3D parallax image card, spec table, VAT calculation, quantity stepper, highlights.
 *
 * OOP Design:
 * - SRP: business logic delegated to services.
 * - Encapsulation: mutable state is `protected`.
 * - DRY: uses {@link VndCurrencyPipe} for currency display.
 */
@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, FooterComponent, AmbientBackgroundComponent, VndCurrencyPipe],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent implements OnInit {
  product: Media | null = null;
  loading = true;
  quantity = 1;
  addedToCart = false;
  isWishlisted = false;
  error = '';

  /** 3D parallax transform for the image card */
  imageTransform = '';

  /** Feature highlights */
  readonly highlights = [
    { icon: 'book',     title: 'Premium Quality',         desc: 'Carefully selected titles from top publishers and distributors worldwide.' },
    { icon: 'sparkles', title: 'Curated Collection',      desc: 'Every item in our store is reviewed and approved by our editorial team.' },
    { icon: 'award',    title: 'Guaranteed Authentic',    desc: 'All products are 100% original and come with quality assurance.' }
  ];

  get vatAmount(): number {
    return (this.product?.currentPrice ?? 0) * 0.1;
  }

  get totalAmount(): number {
    return (this.product?.currentPrice ?? 0) + this.vatAmount;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getProduct(id).subscribe({
      next: (p)  => { this.product = p; this.loading = false; },
      error: ()  => { this.error = 'Product not found.'; this.loading = false; }
    });
  }

  addToCart(): void {
    if (!this.product) return;
    this.cartService.addToCart(this.product, this.quantity);
    this.addedToCart = true;
    setTimeout(() => (this.addedToCart = false), 2500);
  }

  buyNow(): void {
    if (!this.product) return;
    this.cartService.addToCart(this.product, this.quantity);
    this.router.navigate(['/cart']);
  }

  goBack(): void {
    this.router.navigate(['/']);
  }

  protected getFallbackImage(category: string): string {
    const fallbacks: Record<string, string> = {
      Book:      'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80',
      CD:        'https://images.unsplash.com/photo-1619983081563-430f63602796?auto=format&fit=crop&w=600&q=80',
      DVD:       'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80',
      Newspaper: 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=600&q=80',
    };
    return fallbacks[category] ?? fallbacks['Book'];
  }

  changeQty(delta: number): void {
    const max = this.product?.quantityInStock ?? 99;
    this.quantity = Math.max(1, Math.min(this.quantity + delta, max));
  }

  /** 3D parallax effect on mouse move over image card */
  onMouseMove(event: MouseEvent): void {
    const el = event.currentTarget as HTMLElement;
    const rect = el.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width  - 0.5;
    const y = (event.clientY - rect.top)  / rect.height - 0.5;
    const rotX = (-y * 4).toFixed(2);
    const rotY = ( x * 4).toFixed(2);
    this.imageTransform = `perspective(1000px) rotateX(${rotX}deg) rotateY(${rotY}deg)`;
  }

  onMouseLeave(): void {
    this.imageTransform = 'perspective(1000px) rotateX(0deg) rotateY(0deg)';
  }

  protected formatNumber(n: number): string {
    return n.toLocaleString('en-US').replace(/,/g, '.');
  }

  // ===== ISP Type Guard Accessors =====

  protected getSubtitle(): string {
    if (!this.product) return '';
    if (isBook(this.product))      return `By ${this.product.author ?? ''}`;
    if (isCD(this.product))        return `By ${this.product.artist ?? ''}`;
    if (isDVD(this.product))       return `Dir. ${this.product.director ?? ''}`;
    if (isNewspaper(this.product)) return this.product.editorInChief ? `Ed. ${this.product.editorInChief}` : '';
    return '';
  }

  protected getAuthor():        string | null  { return this.product && isBook(this.product)      ? this.product.author ?? null      : null; }
  protected getPublisher():     string | null  { return this.product && isBook(this.product)      ? this.product.publisher ?? null   : null; }
  protected getArtist():        string | null  { return this.product && isCD(this.product)        ? this.product.artist ?? null      : null; }
  protected getDirector():      string | null  { return this.product && isDVD(this.product)       ? this.product.director ?? null    : null; }
  protected getLanguage():      string | null  {
    if (!this.product) return null;
    if (isBook(this.product))      return this.product.language ?? null;
    if (isDVD(this.product))       return this.product.language ?? null;
    return null;
  }
  protected getNumberOfPages(): number | null  { return this.product && isBook(this.product)      ? this.product.numberOfPages ?? null : null; }
  protected getRuntimeMinutes(): number | null { return this.product && isDVD(this.product)       ? this.product.runtimeMinutes ?? null : null; }
  protected getEditorInChief(): string | null  { return this.product && isNewspaper(this.product) ? this.product.editorInChief ?? null : null; }
}
