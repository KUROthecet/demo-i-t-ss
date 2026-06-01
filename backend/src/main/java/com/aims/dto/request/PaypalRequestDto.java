package com.aims.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaypalRequestDto {
	private String intent; // e.g., "CAPTURE" or "AUTHORIZE"
    
    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseUnit {
        private Amount amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        @JsonProperty("currency_code")
        private String currencyCode; // e.g., "USD"
        private String value;        // Must be formatted to 2 decimal places string
    }
}