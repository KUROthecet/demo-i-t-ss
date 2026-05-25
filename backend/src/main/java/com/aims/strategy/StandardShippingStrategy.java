
// Functional Cohesion
// All logic inside the method directly contributes to compute the standard shipping fee based on weight, 
// province, and order total.


package com.aims.strategy;

import org.springframework.stereotype.Component;

@Component
public class StandardShippingStrategy implements ShippingStrategy {

    private static final double HANOI_HCM_BASE_FEE       = 22_000.0;
    private static final double OTHER_BASE_FEE            = 30_000.0;
    private static final double EXTRA_FEE_PER_HALF_KG    = 2_500.0;
    private static final double HANOI_HCM_BASE_WEIGHT_KG = 3.0;
    private static final double OTHER_BASE_WEIGHT_KG      = 0.5;
    private static final double FREE_SHIPPING_THRESHOLD   = 100_000.0;
    private static final double FREE_SHIPPING_DISCOUNT    = 25_000.0;

    @Override
    public double calculate(double weightKg, String province, double orderTotal) {
        double fee;

        if (isHanoiOrHCM(province)) {
            fee = HANOI_HCM_BASE_FEE;
            if (weightKg > HANOI_HCM_BASE_WEIGHT_KG) {
                double extraWeight = weightKg - HANOI_HCM_BASE_WEIGHT_KG;
                long extraUnits = (long) Math.ceil(extraWeight / 0.5);
                fee += extraUnits * EXTRA_FEE_PER_HALF_KG;
            }
        } else {
            fee = OTHER_BASE_FEE;
            if (weightKg > OTHER_BASE_WEIGHT_KG) {
                double extraWeight = weightKg - OTHER_BASE_WEIGHT_KG;
                long extraUnits = (long) Math.ceil(extraWeight / 0.5);
                fee += extraUnits * EXTRA_FEE_PER_HALF_KG;
            }
        }

        if (orderTotal > FREE_SHIPPING_THRESHOLD) {
            fee = Math.max(0.0, fee - FREE_SHIPPING_DISCOUNT);
        }

        return fee;
    }

    private boolean isHanoiOrHCM(String province) {
        if (province == null) return false;
        String normalized = province.trim().toLowerCase();
        return normalized.contains("hanoi")
                || normalized.contains("ha noi")
                || normalized.contains("hà nội")
                || normalized.contains("ho chi minh")
                || normalized.contains("hồ chí minh")
                || normalized.contains("hcm")
                || normalized.equals("hn")
                || normalized.equals("tp.hcm")
                || normalized.equals("tp hcm");
    }
}
