package com.aims.strategy;

public interface ShippingStrategy {
    double calculate(double weightKg, String province, double orderTotal);
}
