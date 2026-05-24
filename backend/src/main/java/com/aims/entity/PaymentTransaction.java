package com.aims.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends Transaction {
    @Column(unique = true, nullable = false)
    private UUID transactionId; 

    private String transactionContent;
    
    private LocalDateTime transactionDatetime;
    
    private String paymentMethod;
    
    private LocalDateTime datetime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public PaymentTransaction(Transaction transaction) {
        this.setAmount(transaction.getAmount());
        this.setTransactionDate(transaction.getTransactionDate());
    }

    public void getTransactionPaymentInfo(String orderId) {
    }

    public void createTransaction(String transactionInfo) {
    }

    public boolean isPayPalPayment() {
        return "PayPal".equalsIgnoreCase(this.paymentMethod);
    }

    public boolean isVietQRPayment() {
        return "VietQR".equalsIgnoreCase(this.paymentMethod);
    }
}