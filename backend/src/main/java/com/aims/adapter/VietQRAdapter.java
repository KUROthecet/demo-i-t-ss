package com.aims.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("vietqrGateway")
public class VietQRAdapter implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(VietQRAdapter.class);

    @Override
    public PaymentResult processPayment(String orderId, long amountVND, String description) {
        log.info("[MOCK VietQR] QR code generated for order: {}, amount: {} VND", orderId, amountVND);
        String qrTransactionId = "VIETQR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String mockQrData = String.format("VIETQR|BANK:970422|ACC:1234567890|AMOUNT:%d|DESC:%s|REF:%s",
                amountVND, description, orderId);
        log.info("[MOCK VietQR] QR data: {}", mockQrData);
        log.info("[MOCK VietQR] Transaction reference: {}", qrTransactionId);
        return new PaymentResult(true, qrTransactionId,
                "VietQR payment initiated. Please scan the QR code to complete payment (mock)");
    }

    @Override
    public RefundResult processRefund(String orderId, long amountVND) {
        // VietQR requires manual bank reconciliation — automatic refunds are not supported
        log.warn("[MOCK VietQR] Automatic refund NOT supported for order: {}. " +
                "Manual refund of {} VND required.", orderId, amountVND);
        return new RefundResult(false, null,
                "VietQR requires manual refund. Please contact the bank to process refund of " +
                amountVND + " VND for order " + orderId);
    }
}
