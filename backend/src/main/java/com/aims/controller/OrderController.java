package com.aims.controller;

import com.aims.dto.request.OrderRequestDto;
import com.aims.dto.response.OrderResponseDto;
import com.aims.enums.OrderStatus;
import com.aims.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto dto) {
        return ResponseEntity.ok(orderService.createOrder(dto));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) OrderStatus status) {
        if (status != null) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status, page, size).map(OrderResponseDto::fromEntity));
        }
        return ResponseEntity.ok(orderService.getAllOrders(page, size).map(OrderResponseDto::fromEntity));
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<OrderResponseDto>> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(orderService.getPendingOrders(page, size).map(OrderResponseDto::fromEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponseDto.fromEntity(orderService.getOrderById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<OrderResponseDto> getOrderByCode(@PathVariable String code) {
        return ResponseEntity.ok(OrderResponseDto.fromEntity(orderService.getOrderByCode(code)));
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByEmail(@RequestParam String email) {
        return ResponseEntity.ok(
            orderService.getOrdersByEmail(email).stream()
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OrderResponseDto> approveOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        return ResponseEntity.ok(OrderResponseDto.fromEntity(orderService.approveOrder(id, performedBy)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderResponseDto> rejectOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Performed-By", defaultValue = "Manager") String performedBy) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return ResponseEntity.ok(OrderResponseDto.fromEntity(orderService.rejectOrder(id, reason, performedBy)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponseDto.fromEntity(orderService.cancelOrder(id)));
    }
}
