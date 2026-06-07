import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Media, PaginatedResponse } from '../models/media.model';

@Injectable({ providedIn: 'root' })
export class MediaApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

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
}
