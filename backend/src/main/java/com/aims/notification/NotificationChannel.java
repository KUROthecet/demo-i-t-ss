package com.aims.notification;

public interface NotificationChannel {
    void send(String recipient, String subject, String content);
}
