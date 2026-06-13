export interface Media {
  readonly id: number;
  readonly barcode: string;
  title: string;
  category: string;
  originalPrice: number;
  currentPrice: number;
  generalDescription: string;
  dimensions?: string;
  weight?: number;
  imageUrl: string;
  quantityInStock: number;
  status: 'ACTIVE' | 'DEACTIVATED';
  supportRushDelivery: boolean;
  attributes?: Record<string, string>;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface CartItem extends Media {
  cartQty: number;
}
