// Data Coupling
// Each method accepts only primitive parameters (String to, String name, String orderCode, long totalAmount,
// boolean refundIssued, String reason, String reason, long amount, String customerName). 
// There is no shared global state, no control flags, and no unnecessary data structures.
package com.aims.service.impl;

import com.aims.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendOrderConfirmation(String to, String name, String orderCode, long totalAmount) {
        log.info("[MOCK EMAIL] Order Confirmation → {} ({}): " +
                 "Thank you for your order {}! Total: {:,} VND. " +
                 "We will process it shortly.", name, to, orderCode, totalAmount);
    }

    @Override
    public void sendOrderApproved(String to, String name, String orderCode) {
        log.info("[MOCK EMAIL] Order Approved → {} ({}): " +
                 "Great news! Your order {} has been approved and is being prepared for delivery.",
                 name, to, orderCode);
    }

    @Override
    public void sendOrderRejected(String to, String name, String orderCode, String reason) {
        log.info("[MOCK EMAIL] Order Rejected → {} ({}): " +
                 "We're sorry, your order {} was rejected. Reason: {}. " +
                 "A refund will be issued if applicable.",
                 name, to, orderCode, reason);
    }

    @Override
    public void sendOrderCancelled(String to, String name, String orderCode, boolean refundIssued) {
        String refundNote = refundIssued
            ? "A refund has been automatically issued."
            : "Please contact support for your refund.";
        log.info("[MOCK EMAIL] Order Cancelled → {} ({}): " +
                 "Your order {} has been cancelled. {}", name, to, orderCode, refundNote);
    }

    @Override
    public void sendPasswordReset(String to, String name, String newPassword) {
        log.info("[MOCK EMAIL] Password Reset → {} ({}): " +
                 "Your temporary password is: {}. Please change it after login.",
                 name, to, newPassword);
    }

    @Override
    public void sendManagerRefundNotification(String to, String orderCode, long amount, String customerName) {
        log.warn("[MOCK EMAIL] Refund Required → Manager ({}): " +
                 "VietQR manual refund needed for order {}. " +
                 "Customer: {}, Amount: {:,} VND.",
                 to, orderCode, customerName, amount);
    }
}
