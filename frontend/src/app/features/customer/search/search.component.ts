import { Component, OnInit, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { CartService } from '../../../core/services/cart.service';
import { Media, isBook, isCD, isDVD, isNewspaper } from '../../../core/models/media.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { FooterComponent } from '../../../shared/footer/footer.component';
import { AmbientBackgroundComponent } from '../../../shared/ambient-background/ambient-background.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

/**
 * SearchComponent — premium search & filter page.
 *
 * Provides: FiltersSidebar (categories + price range slider), results grid,
 * sort dropdown, active filter pills, and empty/initial states.
 *
 * OOP Design:
 * - SRP: handles only search UI and filter state — API calls go through ApiService.
 * - Encapsulation: internal state is `protected`; `MAX_PRICE_VALUE` dynamically computed.
 * - DRY: uses {@link VndCurrencyPipe} instead of a local formatPrice() method.
 * - Bug fix: clearFilters() now uses `this.MAX_PRICE_VALUE` (no magic numbers).
 */
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
    VndCurrencyPipe
  ],
  templateUrl: './search.component.html',
  styleUrl: './search.component.scss'
})
export class SearchComponent implements OnInit {
  protected query              = '';
  protected minPrice           = 0;
  protected maxPrice           = 10_000_000;
  protected selectedCategories: string[] = [];
  protected results: Media[]   = [];
  protected loading            = false;
  protected searched           = false;
  protected sortOrder: 'asc' | 'desc' | '' = '';
  protected error              = '';

  // Pagination state
  protected currentPage = 0;
  protected pageSize = 20;
  protected totalPages = 0;
  protected totalFilteredElements = 0;

  protected isDraggingMin = false;
  protected isDraggingMax = false;

  /** Dynamically computed: highest product price + 2,000,000 VND */
  protected MAX_PRICE_VALUE = 10_000_000;
  readonly PRICE_STEP       = 100_000;

  @ViewChild('sliderRef') sliderRef!: ElementRef<HTMLDivElement>;

  protected get minPercent(): number {
    return (this.minPrice / this.MAX_PRICE_VALUE) * 100;
  }

  protected get maxPercent(): number {
    return (this.maxPrice / this.MAX_PRICE_VALUE) * 100;
  }

  protected get vuBars(): Array<{ isActive: boolean; index: number }> {
    return Array.from({ length: 20 }, (_, i) => {
      const barPercent = (i / 19) * 100;
      const isActive = barPercent >= this.minPercent && barPercent <= this.maxPercent;
      return { isActive, index: i };
    });
  }

  readonly categories = ['Book', 'CD', 'DVD', 'Newspaper'];
  readonly skeletons  = Array(6).fill(0);

  /** Category counts from API facets. */
  private categoryCounts: Record<string, number> = {
    Book: 0, CD: 0, DVD: 0, Newspaper: 0
  };

  constructor(
    private readonly route:       ActivatedRoute,
    private readonly router:      Router,
    private readonly api:         ApiService,
    private readonly cartService: CartService
  ) {}

  ngOnInit(): void {
    this.api.getCatalogStats().subscribe(stats => {
      if (stats) {
        this.categoryCounts = stats;
      }
    });

    this.api.getProducts(1000).subscribe(products => {
      if (products?.length > 0) {
        const highestPrice       = Math.max(...products.map(p => p.currentPrice || 0));
        this.MAX_PRICE_VALUE     = highestPrice + 2_000_000;
        this.maxPrice            = this.MAX_PRICE_VALUE;
      }
      this.route.queryParams.subscribe(params => {
        this.query = params['q'] ?? '';
        if (params['category']) {
          this.selectedCategories = params['category'].split(',');
        } else {
          this.selectedCategories = [];
        }
        this.doSearch();
      });
    });
  }

  protected getTotalCount(): number {
    return Object.values(this.categoryCounts).reduce((a, b) => a + b, 0);
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
    this.error = '';

    this.api.searchProducts(this.query, this.minPrice, this.maxPrice, this.currentPage, this.pageSize).subscribe({
      next: (res: any) => {
        let filtered = res.content;
        
        if (this.selectedCategories.length > 0) {
          filtered = filtered.filter((p: any) => this.selectedCategories.includes(p.category));
        }

        this.results = filtered;
        this.totalFilteredElements = res.totalElements;
        this.totalPages = res.totalPages;
        
        this.searched = true;
        this.loading = false;

        const queryParams: Record<string, string> = {};
        if (this.query) queryParams['q'] = this.query;
        if (this.selectedCategories.length > 0) queryParams['category'] = this.selectedCategories.join(',');

        this.router.navigate([], {
          relativeTo: this.route,
          queryParams,
          replaceUrl: true
        });
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

  protected sortResults(): void {
    if (this.sortOrder === 'asc')  this.results.sort((a, b) => a.currentPrice - b.currentPrice);
    if (this.sortOrder === 'desc') this.results.sort((a, b) => b.currentPrice - a.currentPrice);
  }

  /**
   * Clears all active filters and resets to initial state.
   * DRY Fix: maxPrice is reset to `this.MAX_PRICE_VALUE` — no hardcoded magic number.
   */
  protected clearFilters(): void {
    this.minPrice           = 0;
    this.maxPrice           = this.MAX_PRICE_VALUE; // was previously hardcoded 10000000
    this.selectedCategories = [];
    this.sortOrder          = '';
    this.doSearch();
  }

  protected addToCart(event: MouseEvent, product: Media): void {
    event.stopPropagation();
    event.preventDefault();
    this.cartService.addToCart(product, 1);
  }

  // ===== Creative Price Filter drag handlers =====

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

    if (this.isDraggingMin) {
      this.minPrice = Math.min(value, this.maxPrice - this.PRICE_STEP);
    } else if (this.isDraggingMax) {
      this.maxPrice = Math.max(value, this.minPrice + this.PRICE_STEP);
    }
  }

  @HostListener('document:mouseup')
  handleMouseUp(): void {
    this.isDraggingMin = false;
    this.isDraggingMax = false;
  }

  protected getSinValue(index: number): number {
    return Math.sin(index * 0.5);
  }

  /**
   * Returns the author/creator subtitle for a media item using type guards.
   * ISP: type narrowing via discriminated union — no unsafe property access.
   */
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
