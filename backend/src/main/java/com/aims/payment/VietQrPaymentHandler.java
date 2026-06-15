package com.aims.payment;

import com.aims.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VietQrPaymentHandler implements Payable {

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.VIETQR;
    }

    @Override
    public String initiate(int totalAmount) {
        return "VQR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
