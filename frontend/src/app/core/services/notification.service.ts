import { Injectable, computed, signal, OnDestroy } from '@angular/core';
import { OrderApiService } from './order-api.service';
import { AppConstants } from '../config/app.constants';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

export interface NotificationItem {
  id:           number;
  orderCode:    string;
  customerName: string;
  totalAmount:  number;
  createdAt:    string;
  read:         boolean;
}

const READ_KEY = 'aims_notif_read';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private readonly _items    = signal<NotificationItem[]>([]);
  private _pollSub: Subscription | null = null;

  readonly items       = this._items.asReadonly();
  readonly unreadCount = computed(() => this._items().filter(n => !n.read).length);

  constructor(private readonly orderApi: OrderApiService) {}

  startPolling(): void {
    if (this._pollSub) return;
    this.fetchOnce();
    this._pollSub = interval(AppConstants.NOTIFICATION_POLL_INTERVAL_MS)
      .pipe(switchMap(() => this.orderApi.getPendingOrders(0, 10)))
      .subscribe({ next: page => this.handlePage(page) });
  }

  stopPolling(): void {
    this._pollSub?.unsubscribe();
    this._pollSub = null;
  }

  markAllRead(): void {
    const ids = this._items().map(n => String(n.id));
    this.saveReadIds(new Set(ids));
    this._items.update(list => list.map(n => ({ ...n, read: true })));
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  private fetchOnce(): void {
    this.orderApi.getPendingOrders(0, 10).subscribe({
      next: page => this.handlePage(page)
    });
  }

  private handlePage(page: any): void {
    const readIds = this.loadReadIds();
    const orders: any[] = page?.content ?? page ?? [];
    this._items.set(
      orders.map(o => ({
        id:           o.id,
        orderCode:    o.orderCode,
        customerName: o.customerName,
        totalAmount:  o.totalAmount,
        createdAt:    o.createdAt ?? o.orderDate ?? '',
        read:         readIds.has(String(o.id))
      }))
    );
  }

  private loadReadIds(): Set<string> {
    try {
      const raw = localStorage.getItem(READ_KEY);
      return raw ? new Set(JSON.parse(raw)) : new Set();
    } catch {
      return new Set();
    }
  }

  private saveReadIds(ids: Set<string>): void {
    localStorage.setItem(READ_KEY, JSON.stringify([...ids]));
  }
}
