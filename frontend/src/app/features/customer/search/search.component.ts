import { Component, OnInit, OnDestroy, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, Params } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { MediaApiService } from '../../../core/services/media-api.service';
import { CartService } from '../../../core/services/cart.service';
import { Media, isBook, isCD, isDVD, isNewspaper } from '../../../core/models/media.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { ProductCardComponent } from '../../../shared/product-card/product-card.component';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    NavbarComponent,
    FooterComponent,
    AmbientBackgroundComponent,
    VndCurrencyPipe,
    ProductCardComponent
  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit, OnDestroy {
  protected query              = '';
  protected minPrice           = 0;
  protected maxPrice           = 10_000_000;
  protected selectedCategories: string[] = [];
  protected results: Media[]   = [];
  protected loading            = false;
  protected searched           = false;
  protected sortOrder: 'asc' | 'desc' | '' = '';
  protected error              = '';

  protected currentPage            = 0;
  protected pageSize               = 20;
  protected totalPages             = 0;
  protected totalFilteredElements  = 0;

  protected isDraggingMin = false;
  protected isDraggingMax = false;

  protected MAX_PRICE_VALUE = 10_000_000;
  readonly  PRICE_STEP      = 100_000;

  @ViewChild('sliderRef') sliderRef!: ElementRef<HTMLDivElement>;

  private readonly subscriptions: Subscription[] = [];
  private searchSub: Subscription | null = null;
  private productsReady = false;
  private pendingParams: Params | null = null;

  protected get minPercent(): number {
    return (this.minPrice / this.MAX_PRICE_VALUE) * 100;
  }

  protected get maxPercent(): number {
    return (this.maxPrice / this.MAX_PRICE_VALUE) * 100;
  }

  protected get vuBars(): Array<{ isActive: boolean; index: number }> {
    const bars: Array<{ isActive: boolean; index: number }> = [];
    for (let i = 0; i < 20; i++) {
      bars.push(this.buildVuBar(i));
    }
    return bars;
  }

  private buildVuBar(i: number): { isActive: boolean; index: number } {
    const barPercent = (i / 19) * 100;
    const isActive   = barPercent >= this.minPercent && barPercent <= this.maxPercent;
    return { isActive, index: i };
  }

  readonly categories = ['Book', 'CD', 'DVD', 'Newspaper'];
  readonly skeletons  = Array(6).fill(0);

  private categoryCounts: Record<string, number> = {
    Book: 0, CD: 0, DVD: 0, Newspaper: 0
  };

  constructor(
    private readonly route:       ActivatedRoute,
    private readonly router:      Router,
    private readonly mediaApi:    MediaApiService,
    private readonly cartService: CartService
  ) {}

  ngOnInit(): void {
    const statsSub    = this.mediaApi.getCatalogStats().subscribe(this.onCatalogStatsLoaded.bind(this));
    const productsSub = this.mediaApi.getProducts(1000).subscribe(this.onProductsLoaded.bind(this));
    const qpSub       = this.route.queryParams.subscribe(this.onQueryParamsLoaded.bind(this));
    this.subscriptions.push(statsSub, productsSub, qpSub);
  }

  ngOnDestroy(): void {
    for (const sub of this.subscriptions) {
      sub.unsubscribe();
    }
    if (this.searchSub) {
      this.searchSub.unsubscribe();
    }
  }

  private onCatalogStatsLoaded(stats: any): void {
    if (stats) this.categoryCounts = stats;
  }

  private onProductsLoaded(products: any[]): void {
    if (products?.length > 0) {
      let highestPrice = 0;
      for (const p of products) {
        if ((p.currentPrice || 0) > highestPrice) {
          highestPrice = p.currentPrice || 0;
        }
      }
      this.MAX_PRICE_VALUE = highestPrice + 2_000_000;
      this.maxPrice        = this.MAX_PRICE_VALUE;
    }
    this.productsReady = true;
    if (this.pendingParams !== null) {
      this.applyParamsAndSearch(this.pendingParams);
      this.pendingParams = null;
    }
  }

  private onQueryParamsLoaded(params: Params): void {
    if (!this.productsReady) {
      this.pendingParams = params;
      return;
    }
    this.applyParamsAndSearch(params);
  }

  private applyParamsAndSearch(params: Params): void {
    this.query              = params['q'] ?? '';
    this.selectedCategories = params['category'] ? params['category'].split(',') : [];
    this.doSearch();
  }

  protected getTotalCount(): number {
    let total = 0;
    for (const count of Object.values(this.categoryCounts)) {
      total += count;
    }
    return total;
  }

  protected toggleCategory(cat: string): void {
    const idx = this.selectedCategories.indexOf(cat);
    if (idx > -1) this.selectedCategories.splice(idx, 1);
    else          this.selectedCategories.push(cat);
    this.currentPage = 0;
    this.doSearch();
  }

  protected getCategoryCount(cat: string): number {
    return this.categoryCounts[cat] ?? 0;
  }

  protected doSearch(): void {
    this.loading = true;
    this.error   = '';
    if (this.searchSub) {
      this.searchSub.unsubscribe();
    }
    this.searchSub = this.mediaApi.searchProducts(
      this.query, this.selectedCategories, this.minPrice, this.maxPrice, this.currentPage, this.pageSize
    ).subscribe({
      next:  this.onSearchComplete.bind(this),
      error: this.onSearchError.bind(this)
    });
  }

  private onSearchComplete(res: any): void {
    this.results               = res.content;
    this.totalFilteredElements = res.totalElements;
    this.totalPages            = res.totalPages;
    this.searched              = true;
    this.loading               = false;

    const queryParams: Record<string, string> = {};
    if (this.query) queryParams['q'] = this.query;
    if (this.selectedCategories.length > 0) queryParams['category'] = this.selectedCategories.join(',');

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      replaceUrl: true
    });
  }

  private onSearchError(): void {
    this.results = [];
    this.loading = false;
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.doSearch();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  private comparePriceAsc(a: Media, b: Media): number {
    return a.currentPrice - b.currentPrice;
  }

  private comparePriceDesc(a: Media, b: Media): number {
    return b.currentPrice - a.currentPrice;
  }

  protected sortResults(): void {
    if (this.sortOrder === 'asc')  this.results.sort(this.comparePriceAsc.bind(this));
    if (this.sortOrder === 'desc') this.results.sort(this.comparePriceDesc.bind(this));
  }

  protected clearFilters(): void {
    this.query              = '';
    this.minPrice           = 0;
    this.maxPrice           = this.MAX_PRICE_VALUE;
    this.selectedCategories = [];
    this.sortOrder          = '';
    this.currentPage        = 0;
    this.doSearch();
  }

  protected clearQuery(): void {
    this.query       = '';
    this.currentPage = 0;
    this.doSearch();
  }

  protected addToCart(event: MouseEvent, product: Media): void {
    event.stopPropagation();
    event.preventDefault();
    this.cartService.addToCart(product, 1);
  }

  protected handleMouseDown(thumb: 'min' | 'max'): void {
    if (thumb === 'min') this.isDraggingMin = true;
    else                 this.isDraggingMax = true;
  }

  @HostListener('document:mousemove', ['$event'])
  handleMouseMove(e: MouseEvent): void {
    if (!this.isDraggingMin && !this.isDraggingMax) return;
    if (!this.sliderRef?.nativeElement) return;

    const rect    = this.sliderRef.nativeElement.getBoundingClientRect();
    const percent = Math.max(0, Math.min(100, ((e.clientX - rect.left) / rect.width) * 100));
    const value   = Math.round((percent / 100) * this.MAX_PRICE_VALUE / this.PRICE_STEP) * this.PRICE_STEP;

    if (this.isDraggingMin)      this.minPrice = Math.min(value, this.maxPrice - this.PRICE_STEP);
    else if (this.isDraggingMax) this.maxPrice = Math.max(value, this.minPrice + this.PRICE_STEP);
  }

  @HostListener('document:mouseup')
  handleMouseUp(): void {
    this.isDraggingMin = false;
    this.isDraggingMax = false;
  }

  protected getSinValue(index: number): number {
    return Math.sin(index * 0.5);
  }

  protected getSubtitle(media: Media): string {
    if (isBook(media))      return media.author ?? '';
    if (isCD(media))        return media.artist ?? '';
    if (isDVD(media))       return media.director ?? '';
    if (isNewspaper(media)) return media.editorInChief ? `Ed. ${media.editorInChief}` : '';
    return '';
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
}
