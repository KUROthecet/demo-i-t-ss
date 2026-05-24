package com.aims.repository;

import com.aims.entity.Order;
import com.aims.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByCustomerEmailOrderByOrderDateDesc(String customerEmail);
}
