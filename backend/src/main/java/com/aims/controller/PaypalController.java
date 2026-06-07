package com.aims.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aims.service.PaypalService;
import com.aims.service.OrderService;
import com.aims.entity.Order;

import java.util.Optional;

@RestController
@RequestMapping("/api/paypal")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService  paypalService;
    private final OrderService   orderService;

    @PostMapping("/capture/{paypalOrderId}")
    public ResponseEntity<String> capturePaypalOrder(
            @PathVariable("paypalOrderId") String paypalOrderId
    ) {
        String status = paypalService.captureOrder(paypalOrderId);

        if (status != null && status.startsWith("COMPLETED")) {
            String[] parts = status.split(":");
            String captureId = parts.length > 1 ? parts[1] : null;
            orderService.markOrderPaidByPaypalId(paypalOrderId, captureId);
            return ResponseEntity.ok("COMPLETED");
        } else {
            return ResponseEntity.badRequest().body("Capture failed with status: " + status);
        }
    }
}
