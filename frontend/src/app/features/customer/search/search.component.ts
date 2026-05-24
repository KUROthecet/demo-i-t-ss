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

  protected isDraggingMin = false;
  protected isDraggingMax = false;

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

  private categoryCounts: Record<string, number> = {};

  constructor(
    private readonly route:       ActivatedRoute,
    private readonly router:      Router,
    private readonly api:         ApiService,
    private readonly cartService: CartService
  ) {}

  ngOnInit(): void {
    this.api.getCatalogStats().subscribe({
      next: (stats) => {
        this.categoryCounts  = stats.categoryCounts ?? {};
        this.MAX_PRICE_VALUE = (stats.maxPrice ?? 10_000_000) + 2_000_000;
        this.maxPrice        = this.MAX_PRICE_VALUE;
        this.route.queryParams.subscribe(params => {
          this.query = params['q'] ?? '';
          const catParam = params['category'];
          this.selectedCategories = catParam ? catParam.split(',') : [];
          this.doSearch();
        });
      },
      error: () => {
        // Fallback: proceed with defaults if stats endpoint is unavailable
        this.route.queryParams.subscribe(params => {
          this.query = params['q'] ?? '';
          const catParam = params['category'];
          this.selectedCategories = catParam ? catParam.split(',') : [];
          this.doSearch();
        });
      }
    });
  }

  protected toggleCategory(cat: string): void {
    const idx = this.selectedCategories.indexOf(cat);
    if (idx > -1) this.selectedCategories.splice(idx, 1);
    else          this.selectedCategories.push(cat);
    this.doSearch();
  }

  protected getCategoryCount(cat: string): number {
    return this.categoryCounts[cat] ?? 0;
  }

  protected doSearch(): void {
    this.loading  = true;
    this.searched = true;

    this.api.searchProducts(this.query, this.minPrice, this.maxPrice).subscribe({
      next: (res) => {
        let filtered = res;
        if (this.selectedCategories.length > 0) {
          filtered = filtered.filter(m =>
            this.selectedCategories.some(c => c.toLowerCase() === m.category.toLowerCase())
          );
        }
        this.results = filtered;
        this.sortResults();
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

  protected sortResults(): void {
    if (this.sortOrder === 'asc')  this.results.sort((a, b) => a.currentPrice - b.currentPrice);
    if (this.sortOrder === 'desc') this.results.sort((a, b) => b.currentPrice - a.currentPrice);
  }

  protected clearFilters(): void {
    this.minPrice           = 0;
    this.maxPrice           = this.MAX_PRICE_VALUE;
    this.selectedCategories = [];
    this.sortOrder          = '';
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

  protected getSubtitle(media: Media): string {
    if (isBook(media))      return media.author ?? '';
    if (isCD(media))        return media.artist ?? '';
    if (isDVD(media))       return media.director ?? '';
    if (isNewspaper(media)) return media.editorInChief ? `Ed. ${media.editorInChief}` : '';
    return '';
  }
}
