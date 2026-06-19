package com.aims.strategy;

import com.aims.config.BusinessConstants;
import com.aims.exception.BusinessException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("rushShippingStrategy")
public class RushShippingStrategy implements ShippingStrategy {

    private static final double RUSH_SURCHARGE = 30_000.0;

    private final ShippingStrategy standardShippingStrategy;

    public RushShippingStrategy(
            @Qualifier("standardShippingStrategy") ShippingStrategy standardShippingStrategy) {
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
        String normalized = province.trim();
        return BusinessConstants.RUSH_ELIGIBLE_PROVINCES.stream()
            .anyMatch(e -> e.equalsIgnoreCase(normalized));
    }
}
