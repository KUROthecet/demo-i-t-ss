/* Data Coupling
All methods (calculateFee(), calculate()) accept only primitive parameters 
(double weightKg, String province, double orderTotal, boolean rushDelivery). 
No complex objects are passed between modules
 */

/*
SOLID Violation: Open/Closed Principle (OCP)
Reason Why: Selects shipping fee logic using a hardcoded boolean flag (rushDelivery) rather than relying on polymorphism.
Improvement: Inject a polymorphic ShippingStrategy directly instead of branching on boolean parameters.
*/

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
