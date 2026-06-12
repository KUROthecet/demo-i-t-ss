package com.aims.entity;

import com.aims.entity.state.AbstractOrderState;
import com.aims.entity.state.ApprovedOrderState;
import com.aims.entity.state.CancelledOrderState;
import com.aims.entity.state.PendingOrderState;
import com.aims.entity.state.RejectedOrderState;
import com.aims.enums.OrderStatus;
import com.aims.enums.PaymentMethod;
import com.aims.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
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
    private String paymentCaptureId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime orderDate;
    private LocalDateTime lastUpdated;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "order")
    private List<OrderLine> orderLines = new ArrayList<>();

    @Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AbstractOrderState currentState;

    @PostLoad
    private void initState() {
        this.currentState = createStateFor(this.status);
    }

    private AbstractOrderState createStateFor(OrderStatus orderStatus) {
        return switch (orderStatus) {
            case PENDING_PROCESSING -> new PendingOrderState(this);
            case APPROVED           -> new ApprovedOrderState(this);
            case REJECTED           -> new RejectedOrderState(this);
            case CANCELLED          -> new CancelledOrderState(this);
        };
    }

    private void ensureStateInitialized() {
        if (currentState == null) {
            currentState = createStateFor(status);
        }
    }

    public void transitionState(AbstractOrderState newState) {
        this.currentState = newState;
    }

    @PrePersist
    protected void onCreate() {
        this.orderDate   = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public void approve() {
        ensureStateInitialized();
        currentState.approve();
    }

    public void reject(String reason) {
        ensureStateInitialized();
        currentState.reject(reason);
    }

    public void cancel() {
        ensureStateInitialized();
        currentState.cancel();
    }

    public void markAsPaid(String transactionId, String captureId) {
        this.paymentStatus        = PaymentStatus.PAID;
        this.paymentTransactionId = transactionId;
        this.paymentCaptureId     = captureId;
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
