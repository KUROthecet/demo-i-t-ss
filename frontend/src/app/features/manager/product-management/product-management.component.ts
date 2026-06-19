import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MediaApiService, ManagerStats } from '../../../core/services/media-api.service';
import { Media } from '../../../core/models/media.model';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';
import { AppConstants } from '../../../core/config/app.constants';

@Component({
  selector: 'app-product-management',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, VndCurrencyPipe],
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {
  protected products: Media[]        = [];
  protected statusFilter: 'ALL' | 'ACTIVE' | 'DEACTIVATED' = 'ALL';
  protected loading                  = true;
  protected searchQuery              = '';
  protected selectedIds              = new Set<number>();
  protected dailyDeleteInfo          = { count: 0, remaining: AppConstants.MAX_DAILY_DELETE };
  protected deletingConfirm          = false;
  protected error                    = '';
  protected successMsg               = '';
  protected readonly maxDailyDelete           = AppConstants.MAX_DAILY_DELETE;
  protected readonly deleteWarningThreshold   = AppConstants.DAILY_DELETE_WARNING_THRESHOLD;
  readonly skeletons = Array(8).fill(0);

  protected stats: ManagerStats = { TOTAL: 0, ACTIVE: 0, DEACTIVATED: 0 };
  protected currentPage   = 0;
  protected pageSize      = 20;
  protected totalPages    = 0;
  protected totalElements = 0;

  private pendingDeleteIds: number[] = [];

  constructor(private readonly mediaApi: MediaApiService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadProducts();
    this.mediaApi.getDailyDeleteCount().subscribe(this.onDailyDeleteCountLoaded.bind(this));
  }

  private loadStats(): void {
    this.mediaApi.getManagerStats().subscribe(s => { this.stats = s; });
  }

  private onDailyDeleteCountLoaded(d: any): void {
    this.dailyDeleteInfo = d;
  }

  protected loadProducts(): void {
    this.loading = true;
    this.selectedIds.clear();
    this.mediaApi.getManagerProducts(
      this.searchQuery, [], 0, 2147483647,
      this.currentPage, this.pageSize, this.statusFilter
    ).subscribe({
      next:  this.onProductsLoaded.bind(this),
      error: this.onProductsError.bind(this)
    });
  }

  private onProductsLoaded(data: any): void {
    this.products      = data.content;
    this.totalPages    = data.totalPages;
    this.totalElements = data.totalElements;
    this.loading       = false;
  }

  private onProductsError(): void {
    this.loading = false;
  }

  protected setStatusFilter(f: 'ALL' | 'ACTIVE' | 'DEACTIVATED'): void {
    this.statusFilter = f;
    this.currentPage  = 0;
    this.loadProducts();
  }

  protected changePage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
    }
  }

  protected toggleSelect(id: number): void {
    if (this.selectedIds.has(id)) this.selectedIds.delete(id);
    else                          this.selectedIds.add(id);
  }

  protected selectAll(): void {
    if (this.selectedIds.size === this.products.length) {
      this.selectedIds.clear();
    } else {
      for (const p of this.products) {
        this.selectedIds.add(p.id);
      }
    }
  }

  private onDeleteSuccess(): void {
    this.successMsg = `${this.pendingDeleteIds.length} product(s) processed successfully.`;
    this.selectedIds.clear();
    this.pendingDeleteIds = [];
    this.currentPage = 0;
    this.loadStats();
    this.loadProducts();
    this.mediaApi.getDailyDeleteCount().subscribe(this.onDailyDeleteCountLoaded.bind(this));
    setTimeout(this.clearSuccessMsg.bind(this), 5000);
  }

  private onDeleteError(err: any): void {
    this.error = err.error?.message ?? 'Operation failed.';
  }

  private clearSuccessMsg(): void {
    this.successMsg = '';
  }

  protected reactivateProduct(id: number): void {
    this.mediaApi.reactivateMedia(id).subscribe({
      next:  this.onReactivateSuccess.bind(this),
      error: (err: any) => { this.error = err.error?.message ?? 'Re-activation failed.'; }
    });
  }

  private onReactivateSuccess(): void {
    this.successMsg = 'Product re-activated successfully.';
    this.loadStats();
    this.loadProducts();
    setTimeout(this.clearSuccessMsg.bind(this), 5000);
  }

  protected deleteSelected(): void {
    if (this.selectedIds.size === 0) return;
    if (this.selectedIds.size > AppConstants.MAX_BATCH_DELETE) {
      this.error = `Cannot process more than ${AppConstants.MAX_BATCH_DELETE} products at once. Please deselect some items.`;
      return;
    }
    this.pendingDeleteIds = Array.from(this.selectedIds);
    this.mediaApi.deleteMedia(this.pendingDeleteIds).subscribe({
      next:  this.onDeleteSuccess.bind(this),
      error: this.onDeleteError.bind(this)
    });
    this.deletingConfirm = false;
  }

  protected get subtitleText(): string {
    if (this.statusFilter === 'ALL')         return `${this.stats.TOTAL ?? this.totalElements} products in catalog`;
    if (this.statusFilter === 'ACTIVE')      return `${this.stats.ACTIVE ?? 0} active products`;
    return `${this.stats.DEACTIVATED ?? 0} deactivated products`;
  }
}
