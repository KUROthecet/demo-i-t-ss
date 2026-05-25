// Cohesion Level: Logical Cohesion
// Reason Why: 
// Groups different payment execution endpoints together because they logically handle transaction processing

package com.aims.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aims.dto.request.PaymentRequestDto;
import com.aims.dto.response.PaymentResponseDto;
import com.aims.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processOrderPayment(@RequestBody PaymentRequestDto request) {
        
        PaymentResponseDto response = paymentService.processPayment(request);
        
        return ResponseEntity.ok(response);
    }
}