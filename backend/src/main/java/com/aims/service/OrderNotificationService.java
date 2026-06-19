package com.aims.service;

public interface OrderNotificationService {
    void sendOrderConfirmation(String to, String name, String orderCode, long totalAmount, String transactionRef);
    void sendPaymentConfirmation(String to, String name, String orderCode, long totalAmount,
                                  String transactionId, String captureId, String paidAt);
    void sendOrderApproved(String to, String name, String orderCode);
    void sendOrderRejected(String to, String name, String orderCode, String reason);
    void sendOrderCancelled(String to, String name, String orderCode, boolean refundIssued);
    void sendManagerRefundNotification(String to, String orderCode, long amount, String customerName);
}
