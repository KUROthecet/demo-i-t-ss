package com.aims.controller;

import com.aims.config.BusinessConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConfigController {

    @GetMapping("/api/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of(
            "vatRate",               BusinessConstants.VAT_RATE,
            "freeShippingThreshold", (int) BusinessConstants.FREE_SHIPPING_THRESHOLD,
            "freeShippingCap",       (int) BusinessConstants.FREE_SHIPPING_DISCOUNT,
            "priceMinRatio",         BusinessConstants.PRICE_MIN_RATIO,
            "priceMaxRatio",         BusinessConstants.PRICE_MAX_RATIO,
            "rushEligibleProvinces", BusinessConstants.RUSH_ELIGIBLE_PROVINCES
        ));
    }
}
