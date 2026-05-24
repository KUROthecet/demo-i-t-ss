import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { Order } from '../../../core/models/order.model';
import { AuthService } from '../../../core/services/auth.service';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

@Component({
  selector: 'app-order-processing',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, VndCurrencyPipe],
  templateUrl: './order-processing.component.html',
  styleUrl: './order-processing.component.scss'
})
export class OrderProcessingComponent implements OnInit {
  protected orders: Order[] = [];
  protected loading = true;
  protected filter: 'ALL' | 'PENDING_PROCESSING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' = 'PENDING_PROCESSING';
  protected readonly filterOptions = [
    { value: 'PENDING_PROCESSING' as const, label: 'Pending' },
    { value: 'APPROVED'           as const, label: 'Approved' },
    { value: 'REJECTED'           as const, label: 'Rejected' },
    { value: 'CANCELLED'          as const, label: 'Cancelled' },
    { value: 'ALL'                as const, label: 'All Orders' }
  ];
  protected actionOrderId: number | null = null;
  protected rejectionReason  = '';
  protected showRejectModal  = false;
  protected processing       = false;
  protected error            = '';
  protected readonly performedBy: string;
  readonly skeletons = Array(6).fill(0);

  constructor(
    private readonly api:  ApiService,
    private readonly auth: AuthService
  ) {
    this.performedBy = this.auth.getCurrentUser()?.username ?? 'Manager';
  }

  ngOnInit(): void { this.loadOrders(); }

  protected setFilter(val: 'ALL' | 'PENDING_PROCESSING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'): void {
    this.filter = val;
    this.loadOrders();
  }

  protected loadOrders(): void {
    this.loading = true;
    const obs = this.filter === 'PENDING_PROCESSING' ? this.api.getPendingOrders() : this.api.getOrders();
    obs.subscribe({
      next: (data) => {
        this.orders = this.filter === 'ALL' || this.filter === 'PENDING_PROCESSING'
          ? data
          : data.filter(o => o.status === this.filter);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  protected approve(id: number): void {
    this.processing = true;
    this.api.approveOrder(id).subscribe({
      next:  () =>  { this.loadOrders(); this.processing = false; },
      error: (e) => { this.error = e.error?.message ?? 'Failed to approve.'; this.processing = false; }
    });
  }

  protected openRejectModal(id: number): void {
    this.actionOrderId   = id;
    this.rejectionReason = '';
    this.showRejectModal = true;
  }

  protected confirmReject(): void {
    if (!this.rejectionReason.trim()) { this.error = 'Please provide a rejection reason.'; return; }
    this.processing = true;
    this.api.rejectOrder(this.actionOrderId!, this.rejectionReason).subscribe({
      next:  () =>  { this.showRejectModal = false; this.loadOrders(); this.processing = false; },
      error: (e) => { this.error = e.error?.message ?? 'Failed to reject.'; this.processing = false; }
    });
  }

  protected formatDate(d: string): string { return new Date(d).toLocaleString('vi-VN'); }
}
