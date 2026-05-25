// Cohesion Level: Temporal Cohesion
// Reason Why: 
// Groups datetime updates inside transaction lifecycle hooks to ensure time metrics sync at database execution

package com.aims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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
    private LocalDateTime transactionDate;
    private double amount;
}