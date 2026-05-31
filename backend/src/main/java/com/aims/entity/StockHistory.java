/*
Coupling level: Content coupling
Reason why: Service builds the entity piece-by-piece using setters instead of letting the entity manage its state.
*/

/*
Cohesion Level: Temporal Cohesion
Reason Why: 
Forces timestamp logging to execute in sync with the database save cycle
*/

/*
SOLID Violation: Single Responsibility Principle (SRP)
Reason Why: Couples inventory adjustment values directly to database transactional table configurations
Improvement: Use a clean stock history domain model decoupled from physical database annotations
*/

package com.aims.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Data
@NoArgsConstructor
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;

    private String actionType;

    // Positive = added, Negative = reduced
    private int quantityDelta;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private String performedBy;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
