package com.aims.entity.state;

import com.aims.entity.Order;

public class CancelledOrderState extends AbstractOrderState {

    public CancelledOrderState(Order order) {
        super(order);
    }

    @Override
    public String getStateDescription() {
        return "CANCELLED";
    }
}
