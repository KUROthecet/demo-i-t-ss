package com.aims.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender       mailSender;
    private final EmailTemplateBuilder templateBuilder;

    @Value("${spring.mail.username:aims.noreply@gmail.com}")
    private String fromAddress;

    @Async
    @Override
    public void sendOrderConfirmation(String to, String name, String orderCode, long totalAmount) {
        sendHtml(to, "Order Confirmed — " + orderCode,
            templateBuilder.buildOrderConfirmationHtml(name, orderCode, totalAmount));
    }

    @Async
    @Override
    public void sendOrderApproved(String to, String name, String orderCode) {
        sendHtml(to, "Your Order Has Been Approved — " + orderCode,
            templateBuilder.buildOrderApprovedHtml(name, orderCode));
    }

    @Async
    @Override
    public void sendOrderRejected(String to, String name, String orderCode, String reason) {
        sendHtml(to, "Update on Your Order — " + orderCode,
            templateBuilder.buildOrderRejectedHtml(name, orderCode, reason));
    }

    @Async
    @Override
    public void sendOrderCancelled(String to, String name, String orderCode, boolean refundIssued) {
        sendHtml(to, "Order Cancellation Confirmed — " + orderCode,
            templateBuilder.buildOrderCancelledHtml(name, orderCode, refundIssued));
    }

    @Async
    @Override
    public void sendPasswordReset(String to, String name, String newPassword) {
        sendHtml(to, "Your AIMS Password Has Been Reset",
            templateBuilder.buildPasswordResetHtml(name, newPassword));
    }

    @Async
    @Override
    public void sendNewsletterConfirmation(String to) {
        sendHtml(to, "You're subscribed to AIMS updates",
            templateBuilder.buildNewsletterConfirmationHtml(to));
    }

    @Async
    @Override
    public void sendContactMessage(String senderName, String senderEmail, String subject, String message) {
        sendHtml(fromAddress,
            "[AIMS Contact] " + (subject != null && !subject.isBlank() ? subject : "New message from " + senderName),
            templateBuilder.buildContactMessageHtml(senderName, senderEmail, subject, message));
    }

    @Async
    @Override
    public void sendUserBlocked(String to, String username, String reason) {
        sendHtml(to, "Your AIMS Account Has Been Suspended",
            templateBuilder.buildUserBlockedHtml(username, reason));
    }

    @Async
    @Override
    public void sendUserUnblocked(String to, String username) {
        sendHtml(to, "Your AIMS Account Has Been Reinstated",
            templateBuilder.buildUserUnblockedHtml(username));
    }

    @Async
    @Override
    public void sendUserDeactivated(String to, String username) {
        sendHtml(to, "Your AIMS Account Has Been Deactivated",
            templateBuilder.buildUserDeactivatedHtml(username));
    }

    @Async
    @Override
    public void sendRoleChanged(String to, String username, String newRole) {
        sendHtml(to, "Your AIMS Account Role Has Changed",
            templateBuilder.buildRoleChangedHtml(username, newRole));
    }

    @Async
    @Override
    public void sendManagerRefundNotification(String to, String orderCode, long amount, String customerName) {
        sendHtml(to, "Manual Refund Required — " + orderCode,
            templateBuilder.buildManagerRefundHtml(orderCode, amount, customerName));
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "AIMS Media Store");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("[EMAIL] Sent '{}' to {}", subject, to);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
