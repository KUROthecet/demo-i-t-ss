package com.aims.service;

import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.Transaction;
import com.aims.enums.PaymentMethod;
import com.aims.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaypalService                paypalService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final EmailService                 emailService;

    public String processPayment(PaymentMethod method, int amount) {
        if (method == PaymentMethod.PAYPAL) {
            return paypalService.placeOrder(amount);
        }
        return "";
    }

    @Transactional
    public PaymentTransaction processPaymentTransaction(Order order, int amount, String content, PaymentMethod method) {
        Transaction baseTransaction = new Transaction();
        baseTransaction.setAmount(amount);

        PaymentTransaction payment = new PaymentTransaction(baseTransaction);
        payment.setOrder(order);
        payment.setTransactionContent(content);
        payment.setPaymentMethod(method);

        return paymentTransactionRepository.save(payment);
    }

    public boolean processRefund(Order order, String managerEmail) {
        if (order.getPaymentMethod() == PaymentMethod.PAYPAL && order.getPaymentCaptureId() != null) {
            boolean refunded = paypalService.refundOrder(order.getPaymentCaptureId(), order.getTotalAmount());
            if (refunded) {
                order.markAsRefunded();
            }
            return refunded;
        }
        if (order.getPaymentMethod() == PaymentMethod.VIETQR) {
            emailService.sendManagerRefundNotification(
                    managerEmail, order.getOrderCode(), order.getTotalAmount(), order.getCustomerName());
        }
        return false;
    }
}
