import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class StockApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

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
