package com.aims.strategy;

import com.aims.config.BusinessConstants;
import com.aims.config.ShippingProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@Qualifier("standardShippingStrategy")
public class StandardShippingStrategy implements ShippingStrategy {

    private static final double HANOI_HCM_BASE_FEE       = 22_000.0;
    private static final double OTHER_BASE_FEE            = 30_000.0;
    private static final double EXTRA_FEE_PER_HALF_KG    = 2_500.0;
    private static final double HANOI_HCM_BASE_WEIGHT_KG = 3.0;
    private static final double OTHER_BASE_WEIGHT_KG      = 0.5;

    private final Set<String> tier1Keys;

    public StandardShippingStrategy(ShippingProperties shippingProperties) {
        Set<String> keys = new HashSet<>();
        for (String province : shippingProperties.getTier1Provinces()) {
            keys.add(province.toLowerCase());
        }
        this.tier1Keys = Collections.unmodifiableSet(keys);
    }

    @Override
    public double calculate(double weightKg, String province, double orderTotal) {
        double fee;

        if (isTier1(province)) {
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

    private boolean isTier1(String province) {
        if (province == null) return false;
        return tier1Keys.contains(province.trim().toLowerCase());
    }
}
