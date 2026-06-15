package com.aims.service;

public interface NotificationService {
    void sendOrderConfirmation(String to, String name, String orderCode, long totalAmount, String transactionRef);
    void sendPaymentConfirmation(String to, String name, String orderCode, long totalAmount,
                                  String transactionId, String captureId, String paidAt);
    void sendOrderApproved(String to, String name, String orderCode);
    void sendOrderRejected(String to, String name, String orderCode, String reason);
    void sendOrderCancelled(String to, String name, String orderCode, boolean refundIssued);
    void sendManagerRefundNotification(String to, String orderCode, long amount, String customerName);
    void sendUserBlocked(String to, String username, String reason);
    void sendUserUnblocked(String to, String username);
    void sendUserDeactivated(String to, String username);
    void sendRoleChanged(String to, String username, String newRole);
    void sendPasswordReset(String to, String name, String newPassword);
    void sendContactMessage(String senderName, String senderEmail, String subject, String message);
    void sendNewsletterConfirmation(String to);
}
