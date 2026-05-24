export interface OrderLine {
  id: number;
  media?: { id: number; imageUrl: string; category: string };
  quantity: number;
  unitPrice: number;
  titleSnapshot: string;
}

export interface Order {
  id: number;
  orderCode: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  deliveryAddress: string;
  province: string;
  deliveryNotes: string;
  rushDelivery: boolean;
  preferredDeliveryTime: string;
  subtotal: number;
  vat: number;
  deliveryFee: number;
  rushFee: number;
  totalAmount: number;
  status: 'PENDING_PROCESSING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  rejectionReason: string;
  paymentMethod: 'VIETQR' | 'PAYPAL';
  paymentTransactionId: string;
  paymentStatus: 'PENDING' | 'PAID' | 'REFUNDED';
  orderDate: string;
  lastUpdated: string;
  orderLines?: OrderLine[];
}

export interface OrderRequest {
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  deliveryAddress: string;
  province: string;
  deliveryNotes?: string;
  rushDelivery: boolean;
  preferredDeliveryTime?: string;
  paymentMethod: 'VIETQR' | 'PAYPAL';
  orderLines: { mediaId: number; quantity: number }[];
}

export interface ShippingRequest {
  weight: number;
  province: string;
  orderTotal: number;
  rushDelivery: boolean;
}

export interface ShippingResponse {
  deliveryFee: number;
  rushFee: number;
  rushDelivery: boolean;
}
