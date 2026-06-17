import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserCreateRequest } from '../models/user.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

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

  updateUserRoles(id: number, roles: string[]): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/users/${id}/roles`, { roles });
  }
}
