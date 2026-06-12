import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'orderStatus', standalone: true })
export class OrderStatusPipe implements PipeTransform {
  transform(status: string): { label: string; cssClass: string; color: string } {
    switch (status) {
      case 'PENDING_PROCESSING':
        return { label: 'Pending Review', cssClass: 'status-badge--pending', color: '#f59e0b' };
      case 'APPROVED':
        return { label: 'Approved', cssClass: 'status-badge--approved', color: '#1DB954' };
      case 'REJECTED':
        return { label: 'Rejected', cssClass: 'status-badge--rejected', color: '#ef4444' };
      case 'CANCELLED':
        return { label: 'Cancelled', cssClass: 'status-badge--cancelled', color: '#6b7280' };
      default:
        return { label: status, cssClass: 'status-badge--default', color: '#6b7280' };
    }
  }
}
