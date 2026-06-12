package com.aims.service;

import com.aims.strategy.ShippingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ShippingCalculatorService {

    private final ShippingStrategy standardShippingStrategy;
    private final ShippingStrategy rushShippingStrategy;

    public ShippingCalculatorService(
            @Qualifier("standardShippingStrategy") ShippingStrategy standardShippingStrategy,
            @Qualifier("rushShippingStrategy")     ShippingStrategy rushShippingStrategy) {
        this.standardShippingStrategy = standardShippingStrategy;
        this.rushShippingStrategy     = rushShippingStrategy;
    }

    public double calculateStandardFee(double weightKg, String province, double orderTotal) {
        return standardShippingStrategy.calculate(weightKg, province, orderTotal);
    }

    public double calculateRushFee(double weightKg, String province, double orderTotal) {
        return rushShippingStrategy.calculate(weightKg, province, orderTotal);
    }
}
