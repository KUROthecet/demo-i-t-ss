package com.aims.entity.state;

import com.aims.entity.Order;
import com.aims.enums.OrderStatus;

public class PendingOrderState extends AbstractOrderState {

    public PendingOrderState(Order order) {
        super(order);
    }

    @Override
    public String getStateDescription() {
        return "PENDING_PROCESSING";
    }

    @Override
    public void approve() {
        order.setStatus(OrderStatus.APPROVED);
        order.setCurrentState(new ApprovedOrderState(order));
    }

    @Override
    public void reject(String reason) {
        order.setStatus(OrderStatus.REJECTED);
        order.setRejectionReason(reason);
        order.setCurrentState(new RejectedOrderState(order));
    }

    @Override
    public void cancel() {
        order.setStatus(OrderStatus.CANCELLED);
        order.setCurrentState(new CancelledOrderState(order));
    }
}
