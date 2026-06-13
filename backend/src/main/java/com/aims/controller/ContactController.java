package com.aims.controller;

import com.aims.dto.request.ContactRequestDto;
import com.aims.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> submit(@Valid @RequestBody ContactRequestDto dto) {
        notificationService.sendContactMessage(dto.getName(), dto.getEmail(), dto.getSubject(), dto.getMessage());
        return ResponseEntity.ok().build();
    }
}
