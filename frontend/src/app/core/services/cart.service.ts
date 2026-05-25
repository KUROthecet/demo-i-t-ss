// Stamp Coupling
// The addToCart method receives the entire Media object (which contains over 15 fields). 
// However, the service only needs a small subset of id, currentPrice, title, imageUrl, weight.

// Communicational Cohesion
// All methods work together to manage the shopping cart state (add, update, remove, persist, compute totals).
import { Injectable, computed, signal } from '@angular/core';
import { CartItem, Media } from '../models/media.model';

const CART_KEY = 'aims_cart';

@Injectable({ providedIn: 'root' })
export class CartService {
  private _items = signal<CartItem[]>(this.loadFromStorage());

  items     = this._items.asReadonly();
  itemCount = computed(() => this._items().reduce((sum, i) => sum + i.cartQty, 0));
  subtotal  = computed(() => this._items().reduce((sum, i) => sum + i.currentPrice * i.cartQty, 0));
  vat       = computed(() => Math.round(this.subtotal() * 0.1));
  total     = computed(() => this.subtotal() + this.vat());

  addToCart(product: Media, qty = 1): void {
    const current = this._items();
    const idx = current.findIndex(i => i.id === product.id);
    if (idx >= 0) {
      const updated = [...current];
      updated[idx] = { ...updated[idx], cartQty: updated[idx].cartQty + qty };
      this._items.set(updated);
    } else {
      this._items.set([...current, { ...product, cartQty: qty }]);
    }
    this.persist();
  }

  updateQuantity(productId: number, qty: number): void {
    if (qty <= 0) { this.removeItem(productId); return; }
    this._items.update(items =>
      items.map(i => i.id === productId ? { ...i, cartQty: qty } : i)
    );
    this.persist();
  }

  removeItem(productId: number): void {
    this._items.update(items => items.filter(i => i.id !== productId));
    this.persist();
  }

  clearCart(): void {
    this._items.set([]);
    localStorage.removeItem(CART_KEY);
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
