import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, ShippingRequest, ShippingResponse } from '../models/order.model';
import { PaginatedResponse } from '../models/media.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrderApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  placeOrder(orderReq: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/orders`, orderReq);
  }

  getOrders(page = 0, size = 30, status?: string): Observable<PaginatedResponse<Order>> {
    const params: Record<string, any> = { page, size };
    if (status) params['status'] = status;
    return this.http.get<PaginatedResponse<Order>>(`${this.baseUrl}/orders`, { params });
  }

  getPendingOrders(page = 0, size = 30): Observable<PaginatedResponse<Order>> {
    return this.http.get<PaginatedResponse<Order>>(`${this.baseUrl}/orders/pending`, { params: { page, size } });
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

  approveOrder(id: number, performedBy: string): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/approve`, {}, {
      headers: { 'X-Performed-By': performedBy }
    });
  }

  rejectOrder(id: number, reason: string, performedBy: string): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/reject`, { reason }, {
      headers: { 'X-Performed-By': performedBy }
    });
  }

  cancelOrder(id: number): Observable<Order> {
    return this.http.post<Order>(`${this.baseUrl}/orders/${id}/cancel`, {});
  }

  captureOrder(paypalId: string): Observable<string> {
    return this.http.post<string>(
      `${this.baseUrl}/paypal/capture/${paypalId}`,
      {},
      { responseType: 'text' as 'json' }
    );
  }

  calculateShipping(req: ShippingRequest): Observable<ShippingResponse> {
    return this.http.post<ShippingResponse>(`${this.baseUrl}/shipping/calculate`, req);
  }
}
