package com.aims.entity.state;

import com.aims.entity.Order;
import com.aims.exception.BusinessException;

public abstract class AbstractOrderState {

    protected final Order order;

    protected AbstractOrderState(Order order) {
        this.order = order;
    }

    public abstract String getStateDescription();

    public void approve() {
        throw new BusinessException(
                "Cannot approve order in state: " + getStateDescription());
    }

    public void reject(String reason) {
        throw new BusinessException(
                "Cannot reject order in state: " + getStateDescription());
    }

    public void cancel() {
        throw new BusinessException(
                "Cannot cancel order in state: " + getStateDescription());
    }
}
