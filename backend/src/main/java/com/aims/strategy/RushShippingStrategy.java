/*
Coupling level: Control coupling
Reason why: Passes a boolean flag (rushDelivery) to explicitly dictate the internal if/else execution path.
*/

package com.aims.strategy;

import org.springframework.stereotype.Component;

@Component
public class RushShippingStrategy implements ShippingStrategy {

    // Flat surcharge on top of the standard fee
    private static final double RUSH_SURCHARGE = 30_000.0;

    private final StandardShippingStrategy standardShippingStrategy;

    public RushShippingStrategy(StandardShippingStrategy standardShippingStrategy) {
        this.standardShippingStrategy = standardShippingStrategy;
    }

    @Override
    public double calculate(double weightKg, String province, double orderTotal) {
        double standardFee = standardShippingStrategy.calculate(weightKg, province, orderTotal);
        return standardFee + RUSH_SURCHARGE;
    }
}
