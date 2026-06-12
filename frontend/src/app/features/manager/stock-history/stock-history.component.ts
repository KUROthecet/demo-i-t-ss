import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { StockApiService } from '../../../core/services/stock-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { MediaApiService } from '../../../core/services/media-api.service';

@Component({
  selector: 'app-stock-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './stock-history.component.html',
  styleUrl: './stock-history.component.scss'
})
export class StockHistoryComponent implements OnInit {
  history: any[]  = [];
  loading         = true;
  showAdjustModal = false;
  adjustForm      = { mediaId: null as number | null, quantityDelta: 0, reason: '' };
  adjusting       = false;
  error           = '';
  successMsg      = '';
  performedBy     = this.auth.getCurrentUser()?.username || 'Manager';
  readonly skeletons = Array(6).fill(0);

  productQuery    = '';
  productResults: any[] = [];
  selectedProduct: any  = null;
  searching       = false;

  private readonly searchSubject = new Subject<string>();

  constructor(
    private readonly stockApi:  StockApiService,
    private readonly auth:      AuthService,
    private readonly mediaApi:  MediaApiService
  ) {}

  ngOnInit(): void {
    this.loadHistory();
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        this.searching = true;
        return this.mediaApi.getManagerProducts(q, [], 0, 2147483647, 0, 8);
      })
    ).subscribe({
      next:  res => { this.productResults = res.content ?? []; this.searching = false; },
      error: ()  => { this.searching = false; }
    });
  }

  onProductSearch(): void {
    if (!this.productQuery.trim()) {
      this.productResults  = [];
      this.selectedProduct = null;
      this.adjustForm.mediaId = null;
      return;
    }
    this.searchSubject.next(this.productQuery);
  }

  selectProduct(p: any): void {
    this.selectedProduct    = p;
    this.adjustForm.mediaId = p.id;
    this.productQuery       = p.title;
    this.productResults     = [];
  }

  clearProductSelection(): void {
    this.selectedProduct    = null;
    this.adjustForm.mediaId = null;
    this.productQuery       = '';
    this.productResults     = [];
  }

  openAdjustModal(): void {
    this.showAdjustModal    = true;
    this.selectedProduct    = null;
    this.adjustForm         = { mediaId: null, quantityDelta: 0, reason: '' };
    this.productQuery       = '';
    this.productResults     = [];
    this.error              = '';
  }

  loadHistory(): void {
    this.loading = true;
    this.stockApi.getStockHistory().subscribe({
      next:  this.onHistoryLoaded.bind(this),
      error: this.onHistoryError.bind(this)
    });
  }

  private onHistoryLoaded(data: any): void {
    this.history = data;
    this.loading = false;
  }

  private onHistoryError(): void {
    this.loading = false;
    this.error   = 'Failed to load stock history.';
  }

  submitAdjustment(): void {
    if (!this.adjustForm.mediaId || !this.adjustForm.reason.trim()) {
      this.error = 'Product and reason are required.';
      return;
    }
    this.adjusting = true;
    this.stockApi.adjustStock({
      mediaId:       this.adjustForm.mediaId!,
      quantityDelta: this.adjustForm.quantityDelta,
      reason:        this.adjustForm.reason,
      performedBy:   this.performedBy
    }).subscribe({
      next:  this.onAdjustSuccess.bind(this),
      error: this.onAdjustError.bind(this)
    });
  }

  private onAdjustSuccess(): void {
    this.showAdjustModal = false;
    this.successMsg      = 'Stock adjusted successfully!';
    this.adjustForm      = { mediaId: null, quantityDelta: 0, reason: '' };
    this.loadHistory();
    this.adjusting       = false;
    setTimeout(this.clearSuccessMsg.bind(this), 4000);
  }

  private onAdjustError(e: any): void {
    this.error     = e.error?.message || 'Adjustment failed.';
    this.adjusting = false;
  }

  private clearSuccessMsg(): void {
    this.successMsg = '';
  }

  formatDate(d: string): string { return new Date(d).toLocaleString('vi-VN'); }
}
