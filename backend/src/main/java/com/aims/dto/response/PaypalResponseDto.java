package com.aims.dto.response;

import lombok.Data;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaypalResponseDto {
	private String id;
    private String status;
    
    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PurchaseUnit {
        private Payments payments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payments {
        private List<Capture> captures;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Capture {
        private String id;
    }
}
