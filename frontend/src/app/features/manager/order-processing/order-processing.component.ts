import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OrderApiService } from '../../../core/services/order-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Order } from '../../../core/models/order.model';
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
  protected loading         = true;
  protected currentPage     = 0;
  protected totalPages      = 1;
  protected filter: 'ALL' | 'PENDING_PROCESSING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' = 'PENDING_PROCESSING';
  protected readonly filterOptions = [
    { value: 'PENDING_PROCESSING' as const, label: 'Pending'    },
    { value: 'APPROVED'           as const, label: 'Approved'   },
    { value: 'REJECTED'           as const, label: 'Rejected'   },
    { value: 'CANCELLED'          as const, label: 'Cancelled'  },
    { value: 'ALL'                as const, label: 'All Orders' }
  ];
  protected actionOrderId: number | null = null;
  protected rejectionReason = '';
  protected showRejectModal = false;
  protected processing      = false;
  protected error           = '';
  protected readonly performedBy: string;
  readonly skeletons = Array(6).fill(0);

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

  protected nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  protected prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  protected loadOrders(): void {
    this.loading = true;
    const obs = this.filter === 'PENDING_PROCESSING'
      ? this.orderApi.getPendingOrders(this.currentPage, 30)
      : this.orderApi.getOrders(this.currentPage, 30);
    obs.subscribe({
      next:  this.onOrdersLoaded.bind(this),
      error: this.onOrdersError.bind(this)
    });
  }

  private onOrdersLoaded(data: any): void {
    const content       = data.content as Order[];
    this.totalPages     = data.totalPages;
    this.orders         = (this.filter === 'ALL' || this.filter === 'PENDING_PROCESSING')
      ? content
      : this.filterByStatus(content);
    this.loading = false;
  }

  private onOrdersError(): void {
    this.loading = false;
  }

  private filterByStatus(orders: Order[]): Order[] {
    const result: Order[] = [];
    for (const o of orders) {
      if (o.status === this.filter) result.push(o);
    }
    return result;
  }

  protected approve(id: number): void {
    this.processing = true;
    this.orderApi.approveOrder(id).subscribe({
      next:  this.onApproveSuccess.bind(this),
      error: this.onApproveError.bind(this)
    });
  }

  private onApproveSuccess(): void {
    this.loadOrders();
    this.processing = false;
  }

  private onApproveError(e: any): void {
    this.error      = e.error?.message ?? 'Failed to approve.';
    this.processing = false;
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
      next:  this.onRejectSuccess.bind(this),
      error: this.onRejectError.bind(this)
    });
  }

  private onRejectSuccess(): void {
    this.showRejectModal = false;
    this.loadOrders();
    this.processing      = false;
  }

  private onRejectError(e: any): void {
    this.error      = e.error?.message ?? 'Failed to reject.';
    this.processing = false;
  }

  protected formatDate(d: string): string { return new Date(d).toLocaleString('vi-VN'); }
}
