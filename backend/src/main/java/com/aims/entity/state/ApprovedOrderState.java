package com.aims.entity.state;

import com.aims.entity.Order;

public class ApprovedOrderState extends AbstractOrderState {

    public ApprovedOrderState(Order order) {
        super(order);
    }

    @Override
    public String getStateDescription() {
        return "APPROVED";
    }
}
