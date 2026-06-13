package com.aims.controller;

import com.aims.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NotificationService notificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody SubscribeRequest req) {
        notificationService.sendNewsletterConfirmation(req.getEmail());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class SubscribeRequest {
        @NotBlank
        @Email
        private String email;
    }
}
