package com.aims.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:aims.noreply@gmail.com}")
    private String fromAddress;

    @Async
    @Override
    public void send(String recipient, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "AIMS Media Store");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("[EMAIL] Sent '{}' to {}", subject, recipient);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send '{}' to {}: {}", subject, recipient, e.getMessage());
        }
    }
}
