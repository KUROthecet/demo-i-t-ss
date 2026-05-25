// Data Coupling
// Primitive parameters: double weightKg, String province, double orderTotal, boolean rushDelivery. 

package com.aims.service;

public interface ShippingCalculatorService {
    double calculateFee(double weightKg, String province, double orderTotal, boolean rushDelivery);
}
