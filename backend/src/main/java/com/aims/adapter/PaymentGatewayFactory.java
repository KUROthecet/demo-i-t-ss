package com.aims.adapter;

import com.aims.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayFactory(Map<String, PaymentGateway> gateways) {
        this.gateways = gateways;
    }

    public PaymentGateway getGateway(String method) {
        if (method == null) {
            throw new BusinessException("Payment method must not be null");
        }
        return switch (method.toUpperCase()) {
            case "PAYPAL" -> gateways.get("paypalGateway");
            case "VIETQR" -> gateways.get("vietqrGateway");
            default -> throw new BusinessException("Unsupported payment method: " + method +
                    ". Supported methods: PAYPAL, VIETQR");
        };
    }
}
