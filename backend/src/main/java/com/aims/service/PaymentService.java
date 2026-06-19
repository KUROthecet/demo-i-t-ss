package com.aims.service;

import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.Transaction;
import com.aims.enums.PaymentMethod;
import com.aims.enums.PaymentStatus;
import com.aims.exception.BusinessException;
import com.aims.payment.Payable;
import com.aims.payment.Refundable;
import com.aims.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<PaymentMethod, Payable> handlers;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderNotificationService          notificationService;

    public PaymentService(
            List<Payable> handlerList,
            PaymentTransactionRepository paymentTransactionRepository,
            OrderNotificationService notificationService) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(Payable::supportedMethod, Function.identity()));
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.notificationService = notificationService;
    }

    public String processPayment(PaymentMethod method, int amount) {
        return resolveHandler(method).initiate(amount);
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
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            return true;
        }
        Payable handler = resolveHandler(order.getPaymentMethod());
        if (handler instanceof Refundable refundable) {
            return refundable.refund(order, managerEmail);
        }
        notificationService.sendManagerRefundNotification(
            managerEmail, order.getOrderCode(), order.getTotalAmount(), order.getCustomerName());
        return false;
    }

    private Payable resolveHandler(PaymentMethod method) {
        Payable handler = handlers.get(method);
        if (handler == null) {
            throw new BusinessException("No payment handler registered for method: " + method);
        }
        return handler;
    }
}
