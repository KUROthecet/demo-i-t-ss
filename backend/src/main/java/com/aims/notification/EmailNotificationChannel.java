package com.aims.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EmailNotificationChannel implements NotificationChannel {

    private final RestTemplate restTemplate;

    @Value("${app.mail.api-token:}")
    private String apiToken;

    @Value("${app.mail.inbox-id:}")
    private String inboxId;

    @Value("${app.mail.from-address:aims.noreply@gmail.com}")
    private String fromAddress;

    public EmailNotificationChannel(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Async
    @Override
    public void send(String recipient, String subject, String content) {
        try {
            String url = "https://sandbox.api.mailtrap.io/api/send/" + inboxId;

            Map<String, Object> body = Map.of(
                "from",    Map.of("email", fromAddress, "name", "AIMS Media Store"),
                "to",      List.of(Map.of("email", recipient)),
                "subject", subject,
                "html",    content
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            log.info("[EMAIL] Sent '{}' to {}", subject, recipient);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send '{}' to {}", subject, recipient, e);
        }
    }
}
