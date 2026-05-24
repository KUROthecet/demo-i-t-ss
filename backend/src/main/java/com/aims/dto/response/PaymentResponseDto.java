package com.aims.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private String paymentMethod;
    private String qrCodeData; // Base64 image data from VietQR
    private String paypalApproveUrl; // URL to redirect user to PayPal
    private String message;
}
