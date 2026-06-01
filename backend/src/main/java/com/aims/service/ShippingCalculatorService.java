// Data Coupling
// Primitive parameters: double weightKg, String province, double orderTotal, boolean rushDelivery

/*
SOLID Violation: Open/Closed Principle (OCP)
Reason Why: Selects shipping fee logic using a hardcoded boolean flag rather than relying on polymorphism
Improvement: Inject a polymorphic ShippingStrategy directly instead of branching on boolean parameters
*/

package com.aims.service;

public interface ShippingCalculatorService {
    double calculateFee(double weightKg, String province, double orderTotal, boolean rushDelivery);
}
