package com.aims.entity;

import com.aims.enums.PaymentMethod;
import jakarta.persistence.*;
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

    private String transactionContent;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    public PaymentTransaction(Transaction transaction) {
        super(transaction);
        this.setAmount(transaction.getAmount());
        this.setTransactionDate(transaction.getTransactionDate());
    }

    public boolean isPayPalPayment() {
        return PaymentMethod.PAYPAL.equals(this.paymentMethod);
    }

    public boolean isVietQRPayment() {
        return PaymentMethod.VIETQR.equals(this.paymentMethod);
    }
}
