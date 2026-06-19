package com.aims.service;

public interface UserNotificationService {
    void sendUserBlocked(String to, String username, String reason);
    void sendUserUnblocked(String to, String username);
    void sendUserDeactivated(String to, String username);
    void sendRoleChanged(String to, String username, String newRole);
    void sendPasswordReset(String to, String name, String newPassword);
    void sendNewsletterConfirmation(String to);
    void sendContactMessage(String senderName, String senderEmail, String subject, String message);
}
