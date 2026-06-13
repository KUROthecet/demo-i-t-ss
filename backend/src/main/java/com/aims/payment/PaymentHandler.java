package com.aims.payment;

import com.aims.entity.Order;
import com.aims.enums.PaymentMethod;

public interface PaymentHandler {

    PaymentMethod supportedMethod();

    String initiate(int totalAmount);

    boolean refund(Order order, String managerEmail);
}
