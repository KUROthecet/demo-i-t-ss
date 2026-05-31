/* Data Coupling
Each method accepts only primitive parameters (String to, String name, String orderCode, long totalAmount,
boolean refundIssued, String reason, String reason, long amount, String customerName) 
There is no shared global state, no control flags, and no unnecessary data structures
 */

/*
SOLID Violation: Interface Segregation Principle (ISP)
Reason Why: Combines standard customer notifications with administrator refund notifications into a single interface
Improvement: Split into separate CustomerNotificationService and AdminAlertService interfaces
 */

package com.aims.service;

public interface EmailService {
    void sendOrderConfirmation(String to, String name, String orderCode, long totalAmount);
    void sendOrderApproved(String to, String name, String orderCode);
    void sendOrderRejected(String to, String name, String orderCode, String reason);
    void sendOrderCancelled(String to, String name, String orderCode, boolean refundIssued);
    void sendPasswordReset(String to, String name, String newPassword);
    void sendManagerRefundNotification(String to, String orderCode, long amount, String customerName);
}
