package com.aims.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aims.entity.Order;
import com.aims.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByPaymentTransactionId(String paymentTransactionId);
    
    @Query(
            value = "select distinct o from Order o " +
            "left join fetch o.orderLines ol " +
            "left join fetch ol.media m " +
            "where o.status = :status " +
            "order by o.orderDate desc",
            countQuery = "select count(o) from Order o where o.status = :status"
    )
    org.springframework.data.domain.Page<Order> findByStatusOrderByOrderDateDesc(@Param("status") OrderStatus status, org.springframework.data.domain.Pageable pageable);
    
    @Query(
            value = "select distinct o from Order o " +
            "left join fetch o.orderLines ol " +
            "left join fetch ol.media m " +
            "order by o.orderDate desc",
            countQuery = "select count(o) from Order o"
    )
    org.springframework.data.domain.Page<Order> findAllByOrderByOrderDateDesc(org.springframework.data.domain.Pageable pageable);

    @Query(
        "select distinct o from Order o " +
        "left join fetch o.orderLines ol " +
        "left join fetch ol.media m " +
        "where o.customerEmail = :customerEmail " +
        "order by o.orderDate desc"
    )
    List<Order> findByCustomerEmailOrderByOrderDateDesc(@Param("customerEmail") String customerEmail);

    @Query(
        "select distinct o from Order o " +
        "left join fetch o.orderLines ol " +
        "left join fetch ol.media m " +
        "where o.id = :id"
    )
    Optional<Order> findById(@Param("id") Long id);
}
