package com.aims.payment;

import com.aims.entity.Order;
import com.aims.enums.PaymentMethod;
import com.aims.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VietQrPaymentHandler implements PaymentHandler {

    private final EmailService emailService;

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.VIETQR;
    }

    @Override
    public String initiate(int totalAmount) {
        return "";
    }

    @Override
    public boolean refund(Order order, String managerEmail) {
        emailService.sendManagerRefundNotification(
                managerEmail, order.getOrderCode(), order.getTotalAmount(), order.getCustomerName());
        return false;
    }
}
