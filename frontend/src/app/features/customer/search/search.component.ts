import { Component, OnInit, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MediaApiService } from '../../../core/services/media-api.service';
import { CartService } from '../../../core/services/cart.service';
import { MediaDisplayService } from '../../../core/services/media-display.service';
import { Media } from '../../../core/models/media.model';
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
export class SearchComponent implements OnInit {
  protected query               = '';
  protected minPrice            = 0;
  protected maxPrice            = 10_000_000;
  protected selectedCategories: string[] = [];
  protected results: Media[]    = [];
  protected loading             = false;
  protected searched            = false;
  protected sortOrder: 'asc' | 'desc' | '' = '';
  protected error               = '';

  protected currentPage         = 0;
  protected pageSize            = 20;
  protected totalPages          = 0;
  protected totalFilteredElements = 0;

  protected isDraggingMin       = false;
  protected isDraggingMax       = false;

  protected MAX_PRICE_VALUE     = 10_000_000;

  @ViewChild('sliderRef') sliderRef!: ElementRef<HTMLDivElement>;

  protected get minPercent(): number {
    return (this.minPrice / this.MAX_PRICE_VALUE) * 100;
  }

  protected get maxPercent(): number {
    return (this.maxPrice / this.MAX_PRICE_VALUE) * 100;
  }

  private allPrices: number[]     = [];
  private priceBuckets: number[]  = Array(20).fill(0);

  protected get vuBars(): Array<{ isActive: boolean; index: number; height: number }> {
    const maxCount = Math.max(1, ...this.priceBuckets);
    return Array.from({ length: 20 }, (_, i) => {
      const barPercent = (i / 19) * 100;
      const isActive   = barPercent >= this.minPercent && barPercent <= this.maxPercent;
      const height     = 10 + (this.priceBuckets[i] / maxCount) * 90;
      return { isActive, index: i, height };
    });
  }

  protected get pricePresets(): { label: string; min: number; max: number }[] {
    const step   = this.priceStep;
    const bucket = Math.ceil(this.MAX_PRICE_VALUE / 4 / step) * step;
    return [
      { label: `Under ${this.formatCompact(bucket)}`,                                          min: 0,          max: bucket },
      { label: `${this.formatCompact(bucket)} – ${this.formatCompact(bucket * 2)}`,            min: bucket,     max: bucket * 2 },
      { label: `${this.formatCompact(bucket * 2)} – ${this.formatCompact(bucket * 3)}`,        min: bucket * 2, max: bucket * 3 },
      { label: `Above ${this.formatCompact(bucket * 3)}`,                                      min: bucket * 3, max: 0 },
    ];
  }

  private get priceStep(): number {
    const raw       = this.MAX_PRICE_VALUE / 20;
    const magnitude = Math.pow(10, Math.floor(Math.log10(Math.max(1, raw))));
    const n         = raw / magnitude;
    if (n <= 1) return magnitude;
    if (n <= 2) return 2 * magnitude;
    if (n <= 5) return 5 * magnitude;
    return 10 * magnitude;
  }

  private formatCompact(value: number): string {
    if (value >= 1_000_000) {
      const m = value / 1_000_000;
      return (Number.isInteger(m) ? String(m) : m.toFixed(1)) + 'M';
    }
    if (value >= 1_000) return Math.round(value / 1_000) + 'k';
    return value.toLocaleString();
  }

  private computePriceHistogram(): void {
    const buckets = Array(20).fill(0);
    const max     = this.MAX_PRICE_VALUE;
    for (const price of this.allPrices) {
      const idx = Math.min(19, Math.floor((price / max) * 20));
      buckets[idx]++;
    }
    this.priceBuckets = buckets;
  }

  protected categories: string[]  = [];
  readonly skeletons               = Array(6).fill(0);

  private categoryCounts: Record<string, number> = {};

  constructor(
    private readonly route:        ActivatedRoute,
    private readonly router:       Router,
    private readonly mediaApi:     MediaApiService,
    private readonly cartService:  CartService,
    private readonly mediaDisplay: MediaDisplayService
  ) {}

  ngOnInit(): void {
    this.mediaApi.getCategories().subscribe(cats => {
      this.categories = cats;
    });

    this.mediaApi.getCatalogStats().subscribe(stats => {
      if (stats) this.categoryCounts = stats;
    });

    this.mediaApi.getPriceRange().subscribe(range => {
      if (range?.maxPrice > 0) {
        const highest    = range.maxPrice;
        const buffer     = Math.max(highest * 0.2, 200_000);
        const newMax     = Math.ceil((highest + buffer) / 100_000) * 100_000;
        const wasAtMax   = this.maxPrice >= this.MAX_PRICE_VALUE;
        this.MAX_PRICE_VALUE = newMax;
        if (wasAtMax) this.maxPrice = newMax;
        this.computePriceHistogram();
      }
    });

    this.mediaApi.getPriceHistogram().subscribe(prices => {
      if (prices?.length > 0) {
        const filtered: number[] = [];
        for (const p of prices) {
          if (p > 0) filtered.push(p);
        }
        this.allPrices = filtered;
        this.computePriceHistogram();
      }
    });

    this.route.queryParams.subscribe(params => {
      this.query = params['q'] ?? '';
      if (params['category']) {
        this.selectedCategories = params['category'].split(',');
      } else {
        this.selectedCategories = [];
      }
      this.doSearch();
    });
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

    this.mediaApi.searchProducts(
      this.query, this.selectedCategories, this.minPrice, this.maxPrice,
      this.currentPage, this.pageSize, this.sortOrder
    ).subscribe({
      next: (res: any) => {
        this.results               = res.content;
        this.totalFilteredElements = res.totalElements;
        this.totalPages            = res.totalPages;
        this.searched = true;
        this.loading  = false;

        const queryParams: Record<string, string> = {};
        if (this.query) queryParams['q'] = this.query;
        if (this.selectedCategories.length > 0) queryParams['category'] = this.selectedCategories.join(',');

        this.router.navigate([], { relativeTo: this.route, queryParams, replaceUrl: true });
      },
      error: () => {
        this.results = [];
        this.loading = false;
      }
    });
  }

  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.doSearch();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  protected clearQuery(): void {
    this.query       = '';
    this.currentPage = 0;
    this.doSearch();
  }

  protected onSortChange(): void {
    this.currentPage = 0;
    this.doSearch();
  }

  protected clearFilters(): void {
    this.minPrice           = 0;
    this.maxPrice           = this.MAX_PRICE_VALUE;
    this.selectedCategories = [];
    this.sortOrder          = '';
    this.currentPage        = 0;
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
    const step    = this.priceStep;
    const value   = Math.round((percent / 100) * this.MAX_PRICE_VALUE / step) * step;

    if (this.isDraggingMin) {
      this.minPrice = Math.min(value, this.maxPrice - step);
    } else if (this.isDraggingMax) {
      this.maxPrice = Math.max(value, this.minPrice + step);
    }
  }

  @HostListener('document:mouseup')
  handleMouseUp(): void {
    const wasDragging = this.isDraggingMin || this.isDraggingMax;
    this.isDraggingMin = false;
    this.isDraggingMax = false;
    if (wasDragging) {
      this.currentPage = 0;
      this.doSearch();
    }
  }

  protected applyPricePreset(min: number, max: number): void {
    this.minPrice    = min;
    this.maxPrice    = max === 0 ? this.MAX_PRICE_VALUE : max;
    this.currentPage = 0;
    this.doSearch();
  }

  protected isPresetActive(min: number, max: number): boolean {
    const effectiveMax = max === 0 ? this.MAX_PRICE_VALUE : max;
    return this.minPrice === min && this.maxPrice === effectiveMax;
  }

  protected getSubtitle(media: Media): string {
    return this.mediaDisplay.getSubtitle(media);
  }
}
