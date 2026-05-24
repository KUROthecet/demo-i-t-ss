export interface User {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'PRODUCT_MANAGER';
  status: 'ACTIVE' | 'BLOCKED' | 'DEACTIVATED';
  fullName: string;
  phone: string;
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
  role: 'ADMIN' | 'PRODUCT_MANAGER';
  fullName: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  email: string;
  role: 'ADMIN' | 'PRODUCT_MANAGER';
  fullName: string;
  phone: string;
}
