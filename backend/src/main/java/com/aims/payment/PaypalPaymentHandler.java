package com.aims.payment;

import com.aims.entity.Order;
import com.aims.enums.PaymentMethod;
import com.aims.service.PaypalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaypalPaymentHandler implements PaymentHandler {

    private final PaypalService paypalService;

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public String initiate(int totalAmount) {
        return paypalService.placeOrder(totalAmount);
    }

    @Override
    public boolean refund(Order order, String managerEmail) {
        if (order.getPaymentCaptureId() == null) {
            return false;
        }
        boolean refunded = paypalService.refundOrder(order.getPaymentCaptureId(), order.getTotalAmount());
        if (refunded) {
            order.markAsRefunded();
        }
        return refunded;
    }
}
