import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Media, PaginatedResponse } from '../models/media.model';
import { environment } from '../../../environments/environment';

export interface FieldSchema {
  readonly key: string;
  readonly attributeKey: string;
  readonly label: string;
  readonly type: 'text' | 'number' | 'date' | 'textarea' | 'select';
  readonly options?: string[] | null;
  readonly required: boolean;
}

@Injectable({ providedIn: 'root' })
export class MediaApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getProducts(limit = 20): Observable<Media[]> {
    return this.http.get<Media[]>(`${this.baseUrl}/products`, { params: { limit } });
  }

  getProduct(id: number): Observable<Media> {
    return this.http.get<Media>(`${this.baseUrl}/products/${id}`);
  }

  searchProducts(
    query: string,
    categories: string[],
    minPrice = 0,
    maxPrice = 2147483647,
    page = 0,
    size = 20
  ): Observable<PaginatedResponse<Media>> {
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

  getManagerProducts(
    query: string,
    categories: string[],
    minPrice = 0,
    maxPrice = 2147483647,
    page = 0,
    size = 20
  ): Observable<PaginatedResponse<Media>> {
    let params = new HttpParams()
      .set('query', query)
      .set('minPrice', minPrice)
      .set('maxPrice', maxPrice)
      .set('page', page)
      .set('size', size);
    if (categories && categories.length > 0) {
      params = params.set('category', categories.join(','));
    }
    return this.http.get<PaginatedResponse<Media>>(`${this.baseUrl}/manager/products`, { params });
  }

  getCatalogStats(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.baseUrl}/products/stats`);
  }

  getPriceRange(): Observable<{ minPrice: number; maxPrice: number }> {
    return this.http.get<{ minPrice: number; maxPrice: number }>(`${this.baseUrl}/products/price-range`);
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

  reactivateMedia(id: number): Observable<Media> {
    return this.http.patch<Media>(`${this.baseUrl}/products/${id}/activate`, {});
  }

  getDailyDeleteCount(): Observable<{ count: number; remaining: number }> {
    return this.http.get<{ count: number; remaining: number }>(`${this.baseUrl}/media/daily-delete-count`);
  }

  getSimilarProducts(id: number): Observable<Media[]> {
    return this.http.get<Media[]>(`${this.baseUrl}/products/${id}/similar`);
  }

  getHistoryLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/manager/history`);
  }

  uploadImage(formData: FormData): Observable<{ url: string }> {
    return this.http.post<{ url: string }>(`${this.baseUrl}/upload`, formData);
  }

  getStockBatch(ids: number[]): Observable<Record<number, number>> {
    return this.http.post<Record<number, number>>(`${this.baseUrl}/products/stock-batch`, ids);
  }

  submitContact(payload: { name: string; email: string; subject: string; message: string }): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/contact`, payload);
  }

  subscribeNewsletter(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/newsletter/subscribe`, { email });
  }

  getFieldSchema(): Observable<Record<string, FieldSchema[]>> {
    return this.http.get<Record<string, FieldSchema[]>>(`${this.baseUrl}/media/schema`);
  }
}
