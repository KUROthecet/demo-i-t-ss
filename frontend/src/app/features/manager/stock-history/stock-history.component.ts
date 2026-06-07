import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { StockApiService } from '../../../core/services/stock-api.service';
import { AuthService } from '../../../core/services/auth.service';

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

  constructor(
    private readonly stockApi: StockApiService,
    private readonly auth:     AuthService
  ) {}

  ngOnInit(): void { this.loadHistory(); }

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
  }

  submitAdjustment(): void {
    if (!this.adjustForm.mediaId || !this.adjustForm.reason.trim()) {
      this.error = 'Media ID and reason are required.';
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
