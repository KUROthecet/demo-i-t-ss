package com.aims.service.impl;

import com.aims.adapter.PaymentGateway;
import com.aims.adapter.PaymentGatewayFactory;
import com.aims.adapter.PaymentResult;
import com.aims.adapter.RefundResult;
import com.aims.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayFactory gatewayFactory;

    @Override
    public PaymentResult processPayment(String method, String orderId, long amountVND, String description) {
        PaymentGateway gateway = gatewayFactory.getGateway(method);
        return gateway.processPayment(orderId, amountVND, description);
    }

    @Override
    public RefundResult processRefund(String method, String orderId, long amountVND) {
        PaymentGateway gateway = gatewayFactory.getGateway(method);
        return gateway.processRefund(orderId, amountVND);
    }
}
