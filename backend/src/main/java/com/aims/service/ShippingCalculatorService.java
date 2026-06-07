package com.aims.service;

import com.aims.strategy.RushShippingStrategy;
import com.aims.strategy.StandardShippingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingCalculatorService {

    private final StandardShippingStrategy standardShippingStrategy;
    private final RushShippingStrategy     rushShippingStrategy;

    public double calculateStandardFee(double weightKg, String province, double orderTotal) {
        return standardShippingStrategy.calculate(weightKg, province, orderTotal);
    }

    public double calculateRushFee(double weightKg, String province, double orderTotal) {
        return rushShippingStrategy.calculate(weightKg, province, orderTotal);
    }
}
