package com.aims.payment;

import com.aims.entity.Order;

public interface Refundable {
    boolean refund(Order order, String managerEmail);
}
