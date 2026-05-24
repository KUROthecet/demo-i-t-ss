import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Media, PaginatedResponse } from '../models/media.model';
import { Order, OrderRequest, ShippingRequest, ShippingResponse } from '../models/order.model';
import { User, LoginRequest, LoginResponse, UserCreateRequest } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, data);
  }
  logout(): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/logout`, {});
  }

  getProducts(limit = 20): Observable<Media[]> {
    return this.http.get<Media[]>(`${this.baseUrl}/products`, { params: { limit } });
  }
  getProduct(id: number): Observable<Media> {
    return this.http.get<Media>(`${this.baseUrl}/products/${id}`);
  }
  searchProducts(query: string, categories: string[], minPrice = 0, maxPrice = 2147483647, page = 0, size = 20): Observable<PaginatedResponse<Media>> {
    let params = new HttpParams()
      .set('query', query)
      .set('minPrice', minPrice)
      .set('maxPrice', maxPrice)
      .set('page', page)
      .set('size', size);
    
    if (categories && categories.length > 0) {
      params = params.set('category', categories.join(','));
    }
    return this.http.get<PaginatedResponse<Media>>(`${this.baseUrl}/products/search`, { params });
  }

  getCatalogStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.baseUrl}/products/stats`);
  }
  addMedia(media: Partial<Media>): Observable<Media> {
    return this.http.post<Media>(`${this.baseUrl}/products`, media);
  }
  updateMedia(id: number, media: Partial<Media>): Observable<Media> {
    return this.http.put<Media>(`${this.baseUrl}/products/${id}`, media);
  }
  deleteMedia(ids: number[]): Observable<any> {
    return this.http.delete(`${this.baseUrl}/products`, { body: ids });
  }
  getDailyDeleteCount(): Observable<{ count: number; remaining: number }> {
    return this.http.get<any>(`${this.baseUrl}/media/daily-delete-count`);
  }

  placeOrder(order: OrderRequest): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders`, order);
  }
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/orders`);
  }
  getPendingOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/orders/pending`);
  }
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/orders/${id}`);
  }
  getOrderByCode(code: string): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/orders/code/${code}`);
  }
  getOrdersByEmail(email: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.baseUrl}/orders/by-email`, { params: { email } });
  }
  approveOrder(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/approve`, {});
  }
  rejectOrder(id: number, reason: string): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/reject`, { reason });
  }
  cancelOrder(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/cancel`, {});
  }

  calculateShipping(req: ShippingRequest): Observable<ShippingResponse> {
    return this.http.post<ShippingResponse>(`${this.baseUrl}/shipping/calculate`, req);
  }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }
  createUser(data: UserCreateRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users`, data);
  }
  updateUser(id: number, data: Partial<UserCreateRequest>): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/users/${id}`, data);
  }
  blockUser(id: number, reason: string): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/${id}/block`, { reason });
  }
  unblockUser(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/${id}/unblock`, {});
  }
  deactivateUser(id: number): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/${id}/deactivate`, {});
  }
  resetUserPassword(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/${id}/reset-password`, {});
  }
  changeUserRole(id: number, role: string): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users/${id}/change-role`, { role });
  }

  getHistoryLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/manager/history`);
  }
  getStockHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/stock-history`);
  }
  getStockHistoryForMedia(mediaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/stock-history/media/${mediaId}`);
  }
  adjustStock(data: { mediaId: number; quantityDelta: number; reason: string; performedBy: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/stock-history/adjust`, data);
  }
}
