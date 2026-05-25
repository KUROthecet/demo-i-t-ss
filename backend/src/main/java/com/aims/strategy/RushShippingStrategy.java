// Functional Cohesion
// All logic inside the method directly contributes to compute the rush shipping fee by delegating 
// to the standard strategy and adding a fixed surcharge.
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
