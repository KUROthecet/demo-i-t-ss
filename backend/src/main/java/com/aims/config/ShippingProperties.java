package com.aims.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "aims.shipping")
public class ShippingProperties {

    private List<String> tier1Provinces = new ArrayList<>();

    public List<String> getTier1Provinces() { return tier1Provinces; }
    public void setTier1Provinces(List<String> tier1Provinces) { this.tier1Provinces = tier1Provinces; }
}
