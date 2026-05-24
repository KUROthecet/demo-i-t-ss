package com.aims.adapter;

public interface PaymentGateway {
    PaymentResult processPayment(String orderId, long amountVND, String description);
    RefundResult  processRefund(String orderId, long amountVND);
}
