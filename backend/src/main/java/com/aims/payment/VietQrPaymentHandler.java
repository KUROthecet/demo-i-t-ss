package com.aims.payment;

import com.aims.enums.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class VietQrPaymentHandler implements Payable {

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.VIETQR;
    }

    @Override
    public String initiate(int totalAmount) {
        return "";
    }
}
