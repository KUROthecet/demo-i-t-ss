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
  readonly unreadCount = computed(() => {
    let count = 0;
    for (const n of this._items()) {
      if (!n.read) count++;
    }
    return count;
  });

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
    const ids: string[] = [];
    for (const n of this._items()) {
      ids.push(String(n.id));
    }
    this.saveReadIds(new Set(ids));
    const updated: NotificationItem[] = [];
    for (const n of this._items()) {
      updated.push({ ...n, read: true });
    }
    this._items.set(updated);
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
    const items: NotificationItem[] = [];
    for (const o of orders) {
      items.push({
        id:           o.id,
        orderCode:    o.orderCode,
        customerName: o.customerName,
        totalAmount:  o.totalAmount,
        createdAt:    o.createdAt ?? o.orderDate ?? '',
        read:         readIds.has(String(o.id))
      });
    }
    this._items.set(items);
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
