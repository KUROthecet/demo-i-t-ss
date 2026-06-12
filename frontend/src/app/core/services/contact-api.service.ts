import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ContactApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  submitContact(data: { name: string; email: string; message: string }): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/contact`, data);
  }

  subscribeNewsletter(email: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/newsletter/subscribe`, { email });
  }
}
