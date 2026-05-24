package com.aims.controller;

import com.aims.dto.request.OrderRequestDto;
import com.aims.entity.Order;
import com.aims.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequestDto dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Order> getOrderByCode(@PathVariable String code) {
        return ResponseEntity.ok(orderService.getOrderByCode(code));
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<Order>> getOrdersByEmail(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByEmail(email));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Order> approveOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        return ResponseEntity.ok(orderService.approveOrder(id, performedBy));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Order> rejectOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return ResponseEntity.ok(orderService.rejectOrder(id, reason, "Manager"));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
