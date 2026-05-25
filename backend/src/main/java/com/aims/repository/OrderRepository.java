// Communication Cohesion
// Provide custom query methods (findByOrderCode, findByStatusOrderByOrderDateDesc, 
// findAllByOrderByOrderDateDesc, findByCustomerEmailOrderByOrderDateDesc) working with the Order entity.
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
    
    @Query(
            "select distinct o from Order o " +
            "left join fetch o.orderLines ol " +
            "left join fetch ol.media m " +
            "where o.status = :status " +
            "order by o.orderDate desc"
    )
    List<Order> findByStatusOrderByOrderDateDesc(@Param("status") OrderStatus status);
    
    @Query(
            "select distinct o from Order o " +
            "left join fetch o.orderLines ol " +
            "left join fetch ol.media m " +
            "order by o.orderDate desc"
    )
    List<Order> findAllByOrderByOrderDateDesc();

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
