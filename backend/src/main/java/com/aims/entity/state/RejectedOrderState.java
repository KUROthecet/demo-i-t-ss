package com.aims.entity.state;

import com.aims.entity.Order;

public class RejectedOrderState extends AbstractOrderState {

    public RejectedOrderState(Order order) {
        super(order);
    }

    @Override
    public String getStateDescription() {
        return "REJECTED";
    }
}
