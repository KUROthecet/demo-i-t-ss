package com.aims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;
    
    // BigDecimal guarantees exact precision for financial data
    @Column(nullable = false)
    private int amount;
    
 // 1. Add a Default Constructor (Required by JPA/Hibernate)
    public Transaction() {}

    // 2. Add a "Copy Constructor" to handle mapping
    public Transaction(Transaction source) {
        this.amount = source.getAmount();
        this.transactionDate = source.getTransactionDate();
    }

    // This method runs automatically right before JPA saves the entity
    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
    }
}