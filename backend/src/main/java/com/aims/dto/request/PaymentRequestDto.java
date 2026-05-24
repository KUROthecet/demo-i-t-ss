package com.aims.dto.request;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequestDto {
    private long orderId;
    private BigDecimal amount;
    private String paymentMethod; // "VIETQR" or "PAYPAL"
    private String orderInfo;
}