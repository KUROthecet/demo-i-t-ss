import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { Media } from '../../../core/models/media.model';
import { AuthService } from '../../../core/services/auth.service';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

/**
 * ProductManagementComponent — manager view for browsing, selecting, and deleting products.
 *
 * OOP Design:
 * - SRP: handles only product list display and batch deletion.
 * - Encapsulation: state fields are `protected`.
 * - DRY: delegates currency formatting to {@link VndCurrencyPipe}.
 */
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
  protected dailyDeleteInfo     = { count: 0, remaining: 20 };
  protected deletingConfirm     = false;
  protected error               = '';
  protected successMsg          = '';
  protected readonly performedBy: string;
  readonly skeletons = Array(8).fill(0);

  constructor(
    private readonly api:  ApiService,
    private readonly auth: AuthService
  ) {
    this.performedBy = this.auth.getCurrentUser()?.username ?? 'Manager';
  }

  ngOnInit(): void {
    this.loadProducts();
    this.api.getDailyDeleteCount().subscribe(d => { this.dailyDeleteInfo = d; });
  }

  protected loadProducts(): void {
    this.loading = true;
    this.api.searchProducts(this.searchQuery, [], 0, 9999999).subscribe({
      next:  (data) => { this.products = data.content; this.loading = false; },
      error: ()     => { this.loading = false; }
    });
  }

  protected toggleSelect(id: number): void {
    if (this.selectedIds.has(id)) this.selectedIds.delete(id);
    else this.selectedIds.add(id);
  }

  protected selectAll(): void {
    if (this.selectedIds.size === this.products.length) this.selectedIds.clear();
    else this.products.forEach(p => this.selectedIds.add(p.id));
  }

  protected deleteSelected(): void {
    if (this.selectedIds.size === 0) return;
    const ids = Array.from(this.selectedIds);
    this.api.deleteMedia(ids).subscribe({
      next: () => {
        this.successMsg = `${ids.length} product(s) processed successfully.`;
        this.selectedIds.clear();
        this.loadProducts();
        this.api.getDailyDeleteCount().subscribe(d => { this.dailyDeleteInfo = d; });
        setTimeout(() => this.successMsg = '', 5000);
      },
      error: (err) => { this.error = err.error?.message ?? 'Delete failed.'; }
    });
    this.deletingConfirm = false;
  }
}
