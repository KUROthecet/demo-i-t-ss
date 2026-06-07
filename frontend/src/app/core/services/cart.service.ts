import { Injectable, computed, signal } from '@angular/core';
import { CartItem, Media } from '../models/media.model';

const CART_KEY = 'aims_cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private _items = signal<CartItem[]>(this.loadFromStorage());

  items     = this._items.asReadonly();
  itemCount = computed(this.computeItemCount.bind(this));
  subtotal  = computed(this.computeSubtotal.bind(this));
  vat       = computed(this.computeVat.bind(this));
  total     = computed(this.computeTotal.bind(this));

  addToCart(product: Media, qty = 1): void {
    const current = this._items();
    const idx     = this.findItemIndex(product.id);
    if (idx >= 0) {
      const updated = [...current];
      updated[idx]  = { ...updated[idx], cartQty: updated[idx].cartQty + qty };
      this._items.set(updated);
    } else {
      this._items.set([...current, { ...product, cartQty: qty }]);
    }
    this.persist();
  }

  updateQuantity(productId: number, qty: number): void {
    if (qty <= 0) { this.removeItem(productId); return; }
    const updated: CartItem[] = [];
    for (const item of this._items()) {
      if (item.id === productId) {
        updated.push({ ...item, cartQty: qty });
      } else {
        updated.push(item);
      }
    }
    this._items.set(updated);
    this.persist();
  }

  removeItem(productId: number): void {
    const remaining: CartItem[] = [];
    for (const item of this._items()) {
      if (item.id !== productId) remaining.push(item);
    }
    this._items.set(remaining);
    this.persist();
  }

  clearCart(): void {
    this._items.set([]);
    localStorage.removeItem(CART_KEY);
  }

  private computeItemCount(): number {
    let sum = 0;
    for (const item of this._items()) { sum += item.cartQty; }
    return sum;
  }

  private computeSubtotal(): number {
    let sum = 0;
    for (const item of this._items()) { sum += item.currentPrice * item.cartQty; }
    return sum;
  }

  private computeVat(): number {
    return Math.round(this.subtotal() * 0.1);
  }

  private computeTotal(): number {
    return this.subtotal() + this.vat();
  }

  private findItemIndex(productId: number): number {
    const items = this._items();
    for (let i = 0; i < items.length; i++) {
      if (items[i].id === productId) return i;
    }
    return -1;
  }

  private persist(): void {
    localStorage.setItem(CART_KEY, JSON.stringify(this._items()));
  }

  private loadFromStorage(): CartItem[] {
    try {
      const raw = localStorage.getItem(CART_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }
}
