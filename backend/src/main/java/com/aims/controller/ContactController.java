package com.aims.controller;

import com.aims.dto.request.ContactRequestDto;
import com.aims.entity.ContactSubmission;
import com.aims.repository.ContactSubmissionRepository;
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
    private final ContactSubmissionRepository submissionRepository;

    @PostMapping
    public ResponseEntity<Void> submit(@Valid @RequestBody ContactRequestDto dto) {
        ContactSubmission submission = new ContactSubmission();
        submission.setName(dto.getName());
        submission.setEmail(dto.getEmail());
        submission.setSubject(dto.getSubject());
        submission.setMessage(dto.getMessage());
        submissionRepository.save(submission);
        notificationService.sendContactMessage(dto.getName(), dto.getEmail(), dto.getSubject(), dto.getMessage());
        return ResponseEntity.ok().build();
    }
}
