/*
Coupling level: Content coupling
Reason why: Multiple services completely build the log entity using setters without the entity's control.
*/

/*
Cohesion Level: Temporal Cohesion
Reason Why: 
Uses lifecycle hooks to generate system logging timestamps exactly at the moment database transactions occur
*/

package com.aims.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "history_log")
@Data
@NoArgsConstructor
public class HistoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actionType;
    private String productBarcode;
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
