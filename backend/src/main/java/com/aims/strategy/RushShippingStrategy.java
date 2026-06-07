package com.aims.strategy;

import com.aims.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class RushShippingStrategy implements ShippingStrategy {

    private static final double   RUSH_SURCHARGE           = 30_000.0;
    private static final String[] RUSH_ELIGIBLE_PROVINCES  = { "Hanoi", "Ho Chi Minh City" };

    private final StandardShippingStrategy standardShippingStrategy;

    public RushShippingStrategy(StandardShippingStrategy standardShippingStrategy) {
        this.standardShippingStrategy = standardShippingStrategy;
    }

    @Override
    public double calculate(double weightKg, String province, double orderTotal) {
        if (!isRushEligibleProvince(province)) {
            throw new BusinessException(
                "Rush delivery is only available in Hanoi and Ho Chi Minh City.");
        }
        double standardFee = standardShippingStrategy.calculate(weightKg, province, orderTotal);
        return standardFee + RUSH_SURCHARGE;
    }

    private boolean isRushEligibleProvince(String province) {
        if (province == null) return false;
        for (String eligible : RUSH_ELIGIBLE_PROVINCES) {
            if (eligible.equalsIgnoreCase(province.trim())) return true;
        }
        return false;
    }
}
