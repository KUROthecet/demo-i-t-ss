package com.aims.service;

import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.Transaction;
import com.aims.enums.PaymentMethod;
import com.aims.exception.BusinessException;
import com.aims.payment.PaymentHandler;
import com.aims.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final Map<PaymentMethod, PaymentHandler> handlers;
    private final PaymentTransactionRepository       paymentTransactionRepository;

    public PaymentService(
            List<PaymentHandler> handlerList,
            PaymentTransactionRepository paymentTransactionRepository) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(PaymentHandler::supportedMethod, Function.identity()));
        this.paymentTransactionRepository = paymentTransactionRepository;
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
        return resolveHandler(order.getPaymentMethod()).refund(order, managerEmail);
    }

    private PaymentHandler resolveHandler(PaymentMethod method) {
        PaymentHandler handler = handlers.get(method);
        if (handler == null) {
            throw new BusinessException("No payment handler registered for method: " + method);
        }
        return handler;
    }
}
