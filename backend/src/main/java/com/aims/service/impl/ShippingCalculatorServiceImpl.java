package com.aims.service.impl;

import com.aims.service.ShippingCalculatorService;
import com.aims.strategy.RushShippingStrategy;
import com.aims.strategy.StandardShippingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShippingCalculatorServiceImpl implements ShippingCalculatorService {

    private final StandardShippingStrategy standardShippingStrategy;
    private final RushShippingStrategy     rushShippingStrategy;

    @Override
    public double calculateFee(double weightKg, String province, double orderTotal, boolean rushDelivery) {
        if (rushDelivery) {
            return rushShippingStrategy.calculate(weightKg, province, orderTotal);
        }
        return standardShippingStrategy.calculate(weightKg, province, orderTotal);
    }
}
