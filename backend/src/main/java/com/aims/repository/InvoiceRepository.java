package com.aims.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>{
	
	// Check if an invoice already exists for a specific order
    boolean existsByOrderId(Long orderId);
    
    // Optional: Fetch an invoice by its order ID
    Optional<Invoice> findByOrderId(Long orderId);
	
}
