package com.aims.service;

import com.aims.adapter.PaymentResult;
import com.aims.adapter.RefundResult;

public interface PaymentService {
    PaymentResult processPayment(String method, String orderId, long amountVND, String description);
    RefundResult  processRefund(String method, String orderId, long amountVND);
}
