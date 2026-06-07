package com.aims.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>{
	
    boolean existsByOrderId(Long orderId);

    Optional<Invoice> findByOrderId(Long orderId);
	
}
