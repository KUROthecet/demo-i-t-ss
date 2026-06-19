package com.aims.config;

import java.util.List;

public final class BusinessConstants {

    private BusinessConstants() {}

    public static final double VAT_RATE                = 0.10;
    public static final double FREE_SHIPPING_THRESHOLD = 100_000.0;
    public static final double FREE_SHIPPING_DISCOUNT  = 25_000.0;
    public static final double PRICE_MIN_RATIO         = 0.30;
    public static final double PRICE_MAX_RATIO         = 1.50;

    public static final List<String> RUSH_ELIGIBLE_PROVINCES = List.of("Hanoi", "Ho Chi Minh City");
}
