package com.aims.controller;

import com.aims.dto.request.ShippingRequestDto;
import com.aims.service.ShippingCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingCalculatorService shippingCalculatorService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateShipping(@Valid @RequestBody ShippingRequestDto dto) {
        double standardFee = shippingCalculatorService.calculateStandardFee(
                dto.getWeight(), dto.getProvince(), dto.getOrderTotal());
        double rushFee = 0;
        if (Boolean.TRUE.equals(dto.getRushDelivery())) {
            double rushTotal = shippingCalculatorService.calculateRushFee(
                    dto.getWeight(), dto.getProvince(), dto.getOrderTotal());
            rushFee = rushTotal - standardFee;
        }
        return ResponseEntity.ok(Map.of(
                "deliveryFee", (int) Math.round(standardFee),
                "rushFee",     (int) Math.round(rushFee),
                "rushDelivery", Boolean.TRUE.equals(dto.getRushDelivery())
        ));
    }
}
