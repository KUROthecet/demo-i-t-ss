package com.aims.service;

import com.aims.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

    private final List<NotificationChannel> channels;
    private final EmailTemplateBuilder      templateBuilder;

    @Value("${app.mail.from-address:aims.noreply@gmail.com}")
    private String fromAddress;

    @Override
    public void sendOrderConfirmation(String to, String name, String orderCode, long totalAmount, String transactionRef) {
        dispatch(to, "Order Confirmed — " + orderCode,
            templateBuilder.buildOrderConfirmationHtml(name, orderCode, totalAmount, transactionRef));
    }

    @Override
    public void sendPaymentConfirmation(String to, String name, String orderCode, long totalAmount,
                                         String transactionId, String captureId, String paidAt) {
        dispatch(to, "Payment Confirmed — " + orderCode,
            templateBuilder.buildPaymentConfirmationHtml(name, orderCode, totalAmount, transactionId, captureId, paidAt));
    }

    @Override
    public void sendOrderApproved(String to, String name, String orderCode) {
        dispatch(to, "Your Order Has Been Approved — " + orderCode,
            templateBuilder.buildOrderApprovedHtml(name, orderCode));
    }

    @Override
    public void sendOrderRejected(String to, String name, String orderCode, String reason) {
        dispatch(to, "Update on Your Order — " + orderCode,
            templateBuilder.buildOrderRejectedHtml(name, orderCode, reason));
    }

    @Override
    public void sendOrderCancelled(String to, String name, String orderCode, boolean refundIssued) {
        dispatch(to, "Order Cancellation Confirmed — " + orderCode,
            templateBuilder.buildOrderCancelledHtml(name, orderCode, refundIssued));
    }

    @Override
    public void sendPasswordReset(String to, String name, String newPassword) {
        dispatch(to, "Your AIMS Password Has Been Reset",
            templateBuilder.buildPasswordResetHtml(name, newPassword));
    }

    @Override
    public void sendNewsletterConfirmation(String to) {
        dispatch(to, "You're subscribed to AIMS updates",
            templateBuilder.buildNewsletterConfirmationHtml(to));
    }

    @Override
    public void sendContactMessage(String senderName, String senderEmail, String subject, String message) {
        String s = subject != null && !subject.isBlank() ? subject : "New message from " + senderName;
        dispatch(fromAddress, "[AIMS Contact] " + s,
            templateBuilder.buildContactMessageHtml(senderName, senderEmail, subject, message));
    }

    @Override
    public void sendUserBlocked(String to, String username, String reason) {
        dispatch(to, "Your AIMS Account Has Been Suspended",
            templateBuilder.buildUserBlockedHtml(username, reason));
    }

    @Override
    public void sendUserUnblocked(String to, String username) {
        dispatch(to, "Your AIMS Account Has Been Reinstated",
            templateBuilder.buildUserUnblockedHtml(username));
    }

    @Override
    public void sendUserDeactivated(String to, String username) {
        dispatch(to, "Your AIMS Account Has Been Deactivated",
            templateBuilder.buildUserDeactivatedHtml(username));
    }

    @Override
    public void sendRoleChanged(String to, String username, String newRole) {
        dispatch(to, "Your AIMS Account Role Has Changed",
            templateBuilder.buildRoleChangedHtml(username, newRole));
    }

    @Override
    public void sendManagerRefundNotification(String to, String orderCode, long amount, String customerName) {
        dispatch(to, "Manual Refund Required — " + orderCode,
            templateBuilder.buildManagerRefundHtml(orderCode, amount, customerName));
    }

    private void dispatch(String recipient, String subject, String content) {
        for (NotificationChannel channel : channels) {
            channel.send(recipient, subject, content);
        }
    }
}
