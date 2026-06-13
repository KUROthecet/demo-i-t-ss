import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MediaApiService } from '../../../core/services/media-api.service';
import { AuthService } from '../../../core/services/auth.service';
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
  protected products: Media[]   = [];
  protected loading             = true;
  protected searchQuery         = '';
  protected selectedIds         = new Set<number>();
  protected dailyDeleteInfo     = { count: 0, remaining: AppConstants.MAX_DAILY_DELETE };
  protected deletingConfirm     = false;
  protected error               = '';
  protected successMsg          = '';
  protected readonly performedBy: string;
  protected readonly maxDailyDelete            = AppConstants.MAX_DAILY_DELETE;
  protected readonly deleteWarningThreshold    = AppConstants.DAILY_DELETE_WARNING_THRESHOLD;
  readonly skeletons = Array(8).fill(0);

  private pendingDeleteIds: number[] = [];

  constructor(
    private readonly mediaApi: MediaApiService,
    private readonly auth:     AuthService
  ) {
    this.performedBy = this.auth.getCurrentUser()?.username ?? 'Manager';
  }

  ngOnInit(): void {
    this.loadProducts();
    this.mediaApi.getDailyDeleteCount().subscribe(this.onDailyDeleteCountLoaded.bind(this));
  }

  private onDailyDeleteCountLoaded(d: any): void {
    this.dailyDeleteInfo = d;
  }

  protected loadProducts(): void {
    this.loading = true;
    this.mediaApi.getManagerProducts(this.searchQuery, [], 0, 9999999).subscribe({
      next:  this.onProductsLoaded.bind(this),
      error: this.onProductsError.bind(this)
    });
  }

  private onProductsLoaded(data: any): void {
    this.products = data.content;
    this.loading  = false;
  }

  private onProductsError(): void {
    this.loading = false;
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
    this.loadProducts();
    this.mediaApi.getDailyDeleteCount().subscribe(this.onDailyDeleteCountLoaded.bind(this));
    setTimeout(this.clearSuccessMsg.bind(this), 5000);
  }

  private onDeleteError(err: any): void {
    this.error = err.error?.message ?? 'Delete failed.';
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
    this.loadProducts();
    setTimeout(this.clearSuccessMsg.bind(this), 5000);
  }

  protected deleteSelected(): void {
    if (this.selectedIds.size === 0) return;
    if (this.selectedIds.size > AppConstants.MAX_BATCH_DELETE) {
      this.error = `Cannot delete more than ${AppConstants.MAX_BATCH_DELETE} products at once. Please deselect some items.`;
      return;
    }
    this.pendingDeleteIds = Array.from(this.selectedIds);
    this.mediaApi.deleteMedia(this.pendingDeleteIds).subscribe({
      next:  this.onDeleteSuccess.bind(this),
      error: this.onDeleteError.bind(this)
    });
    this.deletingConfirm = false;
  }
}
