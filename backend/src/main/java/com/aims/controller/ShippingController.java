package com.aims.controller;

import com.aims.dto.request.ShippingRequestDto;
import com.aims.service.ShippingCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ShippingController {

    private final ShippingCalculatorService shippingCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateShipping(@RequestBody ShippingRequestDto dto) {
        double standardFee = shippingCalculatorService.calculateFee(
            dto.getWeight(), dto.getProvince(), dto.getOrderTotal(), false
        );
        double rushFee = 0;
        if (Boolean.TRUE.equals(dto.getRushDelivery())) {
            double totalFee = shippingCalculatorService.calculateFee(
                dto.getWeight(), dto.getProvince(), dto.getOrderTotal(), true
            );
            rushFee = totalFee - standardFee;
        }
        return ResponseEntity.ok(Map.of(
            "deliveryFee", (int) Math.round(standardFee),
            "rushFee", (int) Math.round(rushFee),
            "rushDelivery", Boolean.TRUE.equals(dto.getRushDelivery())
        ));
    }
}
