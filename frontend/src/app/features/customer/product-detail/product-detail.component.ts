import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AppConstants } from '../../../core/config/app.constants';
import { MediaApiService } from '../../../core/services/media-api.service';
import { MediaDisplayService } from '../../../core/services/media-display.service';
import { CartService } from '../../../core/services/cart.service';
import { Media } from '../../../core/models/media.model';
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
  error                      = '';

  get vatAmount(): number {
    return (this.product?.currentPrice ?? 0) * AppConstants.VAT_RATE;
  }

  get totalAmount(): number {
    return (this.product?.currentPrice ?? 0) + this.vatAmount;
  }

  get productAttributes(): [string, string][] {
    if (!this.product?.attributes) return [];
    return Object.entries(this.product.attributes).filter(([, v]) => v != null && v !== '');
  }

  constructor(
    private readonly route:        ActivatedRoute,
    private readonly router:       Router,
    private readonly mediaApi:     MediaApiService,
    private readonly cartService:  CartService,
    private readonly mediaDisplay: MediaDisplayService
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
      error: () => {}
    });
  }

  private onProductError(): void {
    this.error   = 'Product not found.';
    this.loading = false;
  }

  private onSimilarProductsLoaded(similar: Media[]): void {
    this.similarProducts = similar;
  }

  addToCart(): void {
    if (!this.product) return;
    const added = this.cartService.addToCart(this.product, this.quantity);
    if (added) {
      this.addedToCart = true;
      setTimeout(this.clearAddedToCart.bind(this), 2500);
    }
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

  changeQty(delta: number): void {
    const max   = this.product?.quantityInStock ?? 99;
    this.quantity = Math.max(1, Math.min(this.quantity + delta, max));
  }

  protected formatNumber(n: number): string {
    return n.toLocaleString('en-US').replace(/,/g, '.');
  }

  protected getSubtitle(): string {
    if (!this.product) return '';
    return this.mediaDisplay.getSubtitle(this.product);
  }
}
