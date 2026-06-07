import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MediaApiService } from '../../../core/services/media-api.service';
import { CartService } from '../../../core/services/cart.service';
import { Media, isBook, isCD, isDVD, isNewspaper } from '../../../core/models/media.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { ProductCardComponent } from '../../../shared/product-card/product-card.component';
import { ProductImageFrameComponent } from './product-image-frame/product-image-frame.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NavbarComponent,
    FooterComponent,
    AmbientBackgroundComponent,
    VndCurrencyPipe,
    ProductCardComponent,
    ProductImageFrameComponent
  ],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent implements OnInit {
  product: Media | null      = null;
  similarProducts: Media[]   = [];
  loading                    = true;
  quantity                   = 1;
  addedToCart                = false;
  isWishlisted               = false;
  error                      = '';

  get vatAmount(): number {
    return (this.product?.currentPrice ?? 0) * 0.1;
  }

  get totalAmount(): number {
    return (this.product?.currentPrice ?? 0) + this.vatAmount;
  }

  constructor(
    private readonly route:       ActivatedRoute,
    private readonly router:      Router,
    private readonly mediaApi:    MediaApiService,
    private readonly cartService: CartService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(this.onRouteParamsChanged.bind(this));
  }

  private onRouteParamsChanged(params: any): void {
    const id = Number(params.get('id'));
    this.loading         = true;
    this.product         = null;
    this.similarProducts = [];
    this.quantity        = 1;

    this.mediaApi.getProduct(id).subscribe({
      next:  this.onProductLoaded.bind(this),
      error: this.onProductError.bind(this)
    });
  }

  private onProductLoaded(p: Media): void {
    this.product = p;
    this.loading = false;
    this.mediaApi.getSimilarProducts(p.id).subscribe({
      next:  this.onSimilarProductsLoaded.bind(this),
      error: this.onSimilarProductsError.bind(this)
    });
  }

  private onProductError(): void {
    this.error   = 'Product not found.';
    this.loading = false;
  }

  private onSimilarProductsLoaded(similar: Media[]): void {
    this.similarProducts = similar;
  }

  private onSimilarProductsError(): void {}

  addToCart(): void {
    if (!this.product) return;
    this.cartService.addToCart(this.product, this.quantity);
    this.addedToCart = true;
    setTimeout(this.clearAddedToCart.bind(this), 2500);
  }

  private clearAddedToCart(): void {
    this.addedToCart = false;
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
    const max  = this.product?.quantityInStock ?? 99;
    this.quantity = Math.max(1, Math.min(this.quantity + delta, max));
  }

  protected formatNumber(n: number): string {
    return n.toLocaleString('en-US').replace(/,/g, '.');
  }

  protected getSubtitle(): string {
    if (!this.product) return '';
    if (isBook(this.product))      return `By ${this.product.author ?? ''}`;
    if (isCD(this.product))        return `By ${this.product.artist ?? ''}`;
    if (isDVD(this.product))       return `Dir. ${this.product.director ?? ''}`;
    if (isNewspaper(this.product)) return this.product.editorInChief ? `Ed. ${this.product.editorInChief}` : '';
    return '';
  }

  protected getAuthor():         string | null { return this.product && isBook(this.product)      ? this.product.author ?? null      : null; }
  protected getPublisher():      string | null { return this.product && isBook(this.product)      ? this.product.publisher ?? null   : null; }
  protected getArtist():         string | null { return this.product && isCD(this.product)        ? this.product.artist ?? null      : null; }
  protected getDirector():       string | null { return this.product && isDVD(this.product)       ? this.product.director ?? null    : null; }
  protected getLanguage():       string | null {
    if (!this.product) return null;
    if (isBook(this.product)) return this.product.language ?? null;
    if (isDVD(this.product))  return this.product.language ?? null;
    return null;
  }
  protected getNumberOfPages():  number | null { return this.product && isBook(this.product)      ? this.product.numberOfPages ?? null  : null; }
  protected getRuntimeMinutes(): number | null { return this.product && isDVD(this.product)       ? this.product.runtimeMinutes ?? null : null; }
  protected getEditorInChief():  string | null { return this.product && isNewspaper(this.product) ? this.product.editorInChief ?? null  : null; }
}
