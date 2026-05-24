package com.aims.service;

import com.aims.dto.request.OrderRequestDto;
import com.aims.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderRequestDto dto);
    Order getOrderById(Long id);
    Order getOrderByCode(String code);
    List<Order> getPendingOrders();
    List<Order> getAllOrders();
    List<Order> getOrdersByEmail(String email);
    Order approveOrder(Long id, String performedBy);
    Order rejectOrder(Long id, String reason, String performedBy);
    Order cancelOrder(Long id);
}
