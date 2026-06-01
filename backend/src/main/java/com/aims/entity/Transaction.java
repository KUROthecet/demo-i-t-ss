/*
 * SOLID Principles Analysis:
 * - Violated Principle(s): SRP
 * - Reason and Impact: Combines business logic and attribute validation with relational database persistence mapping configurations.
 * - Improvement Direction: Separate business models from database configurations.
 */
/*
Coupling level: Content coupling
Reason why: Subclass improperly shadows the parent's ID and manually extracts/sets parent fields.
*/

/*
Cohesion Level: Temporal Cohesion
Reason Why: Groups datetime updates inside transaction lifecycle hooks to ensure time metrics sync at database execution
*/

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