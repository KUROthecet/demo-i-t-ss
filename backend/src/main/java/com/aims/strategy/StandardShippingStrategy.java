package com.aims.strategy;

import com.aims.config.BusinessConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Qualifier("standardShippingStrategy")
public class StandardShippingStrategy implements ShippingStrategy {

    private static final double HANOI_HCM_BASE_FEE       = 22_000.0;
    private static final double OTHER_BASE_FEE            = 30_000.0;
    private static final double EXTRA_FEE_PER_HALF_KG    = 2_500.0;
    private static final double HANOI_HCM_BASE_WEIGHT_KG = 3.0;
    private static final double OTHER_BASE_WEIGHT_KG      = 0.5;

    private enum ShippingTier { TIER_1, TIER_2 }

    private static final Map<String, ShippingTier> PROVINCE_TIERS;
    static {
        java.util.Map<String, ShippingTier> m = new java.util.HashMap<>();
        for (String key : java.util.List.of(
                "hanoi", "ha noi", "hà nội",
                "ho chi minh", "hồ chí minh", "hcm", "hn", "tp.hcm", "tp hcm")) {
            m.put(key, ShippingTier.TIER_1);
        }
        PROVINCE_TIERS = java.util.Collections.unmodifiableMap(m);
    }

    @Override
    public double calculate(double weightKg, String province, double orderTotal) {
        double fee;

        if (getTier(province) == ShippingTier.TIER_1) {
            fee = HANOI_HCM_BASE_FEE;
            if (weightKg > HANOI_HCM_BASE_WEIGHT_KG) {
                double extraWeight = weightKg - HANOI_HCM_BASE_WEIGHT_KG;
                long   extraUnits  = (long) Math.ceil(extraWeight / 0.5);
                fee += extraUnits * EXTRA_FEE_PER_HALF_KG;
            }
        } else {
            fee = OTHER_BASE_FEE;
            if (weightKg > OTHER_BASE_WEIGHT_KG) {
                double extraWeight = weightKg - OTHER_BASE_WEIGHT_KG;
                long   extraUnits  = (long) Math.ceil(extraWeight / 0.5);
                fee += extraUnits * EXTRA_FEE_PER_HALF_KG;
            }
        }

        if (orderTotal > BusinessConstants.FREE_SHIPPING_THRESHOLD) {
            fee = Math.max(0.0, fee - BusinessConstants.FREE_SHIPPING_DISCOUNT);
        }

        return fee;
    }

    private ShippingTier getTier(String province) {
        if (province == null) return ShippingTier.TIER_2;
        return PROVINCE_TIERS.getOrDefault(province.trim().toLowerCase(), ShippingTier.TIER_2);
    }
}
