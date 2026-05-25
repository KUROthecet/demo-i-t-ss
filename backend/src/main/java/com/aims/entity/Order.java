/*
Coupling level: Content Coupling 
Reason why: Service acts as an overarching class, bypassing encapsulation to directly set the entities' internal state.
*/
/*
Cohesion Level: Temporal Cohesion
Reason Why: 
The fields orderDate and lastUpdated are grouped within lifecycle hooks
Since their database values must generate at the same point in time during persistence
*/

package com.aims.entity;

import com.aims.enums.OrderStatus;
import com.aims.enums.PaymentMethod;
import com.aims.enums.PaymentStatus;
import com.aims.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderCode;

    @NotBlank
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    @NotBlank
    private String province;

    @Column(columnDefinition = "TEXT")
    private String deliveryNotes;

    private boolean rushDelivery;
    private String  preferredDeliveryTime;

    private int subtotal;
    private int vat;
    private int deliveryFee;
    private int rushFee;
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PROCESSING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    private String paymentTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime orderDate;
    private LocalDateTime lastUpdated;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "order")
    private List<OrderLine> orderLines = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.orderDate   = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    // UC009: only PENDING_PROCESSING orders can be approved
    public void approve() {
        if (this.status != OrderStatus.PENDING_PROCESSING) {
            throw new BusinessException(
                "Only PENDING_PROCESSING orders can be approved. Current status: " + this.status);
        }
        this.status = OrderStatus.APPROVED;
    }

    // UC010: only PENDING_PROCESSING orders can be rejected
    public void reject(String reason) {
        if (this.status != OrderStatus.PENDING_PROCESSING) {
            throw new BusinessException(
                "Only PENDING_PROCESSING orders can be rejected. Current status: " + this.status);
        }
        this.status          = OrderStatus.REJECTED;
        this.rejectionReason = reason;
    }

    // UC011: customer can only cancel before approval
    public void cancel() {
        if (this.status != OrderStatus.PENDING_PROCESSING) {
            throw new BusinessException(
                "Only PENDING_PROCESSING orders can be cancelled. Current status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void markAsPaid() {
        this.paymentStatus        = PaymentStatus.PAID;
    }

    public void markAsRefunded() {
        this.paymentStatus = PaymentStatus.REFUNDED;
    }

    public boolean isPending() {
        return this.status == OrderStatus.PENDING_PROCESSING;
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING_PROCESSING;
    }
}
