import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/services/order-api.service';
import { Order } from '../../../core/models/order.model';
import { AuthService } from '../../../core/services/auth.service';
import { AppConstants } from '../../../core/config/app.constants';
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
  protected loading          = true;
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
  protected currentPage      = 0;
  protected totalPages       = 0;
  protected readonly performedBy: string;
  readonly skeletons = Array(6).fill(0);
  readonly pageSize  = AppConstants.PAGE_SIZE_PENDING_ORDERS;

  constructor(
    private readonly orderApi: OrderApiService,
    private readonly auth:     AuthService
  ) {
    this.performedBy = this.auth.getCurrentUser()?.username ?? 'Manager';
  }

  ngOnInit(): void { this.loadOrders(); }

  protected setFilter(val: 'ALL' | 'PENDING_PROCESSING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'): void {
    this.filter      = val;
    this.currentPage = 0;
    this.loadOrders();
  }

  protected prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  protected nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  protected loadOrders(): void {
    this.loading = true;
    const status = this.filter === 'ALL' ? undefined : this.filter;
    this.orderApi.getOrders(this.currentPage, this.pageSize, status).subscribe({
      next:  (data) => { this.orders = data.content; this.totalPages = data.totalPages; this.loading = false; },
      error: ()     => { this.loading = false; }
    });
  }

  protected approve(id: number): void {
    this.processing = true;
    this.orderApi.approveOrder(id).subscribe({
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
    this.orderApi.rejectOrder(this.actionOrderId!, this.rejectionReason).subscribe({
      next:  () =>  { this.showRejectModal = false; this.loadOrders(); this.processing = false; },
      error: (e) => { this.error = e.error?.message ?? 'Failed to reject.'; this.processing = false; }
    });
  }

  protected formatDate(d: string): string { return new Date(d).toLocaleString('vi-VN'); }
}
