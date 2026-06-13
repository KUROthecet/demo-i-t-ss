package com.aims.payment;

import com.aims.enums.PaymentMethod;

public interface Payable {
    PaymentMethod supportedMethod();
    String initiate(int totalAmount);
}
