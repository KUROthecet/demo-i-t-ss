package com.aims.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("paypalGateway")
public class PayPalAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PayPalAdapter.class);

    @Override
    public PaymentResult processPayment(String orderId, long amountVND, String description) {
        log.info("[MOCK PayPal] Processing payment for order: {}, amount: {} VND, description: {}",
                orderId, amountVND, description);
        String transactionId = "PAYPAL-TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        log.info("[MOCK PayPal] Payment successful. Transaction ID: {}", transactionId);
        return new PaymentResult(true, transactionId, "PayPal payment processed successfully (mock)");
    }

    @Override
    public RefundResult processRefund(String orderId, long amountVND) {
        log.info("[MOCK PayPal] Auto-refund processed for order: {}, amount: {} VND", orderId, amountVND);
        String refundId = "PAYPAL-REFUND-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        log.info("[MOCK PayPal] Refund successful. Refund ID: {}", refundId);
        return new RefundResult(true, refundId, "PayPal refund processed automatically (mock)");
    }
}
