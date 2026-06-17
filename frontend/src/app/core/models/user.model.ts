export type UserRole = 'ADMIN' | 'PRODUCT_MANAGER';

export interface User {
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
  status: 'ACTIVE' | 'BLOCKED' | 'DEACTIVATED';
  fullName: string;
  phone: string;
  avatarUrl?: string;
  blockReason?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  roles: UserRole[];
  fullName: string;
  avatarUrl?: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  email: string;
  roles: UserRole[];
  fullName: string;
  phone: string;
  avatarUrl?: string;
}
