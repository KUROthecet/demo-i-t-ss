package com.aims.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aims.entity.Order;
import com.aims.entity.PaymentTransaction;
import com.aims.entity.Transaction;
import com.aims.enums.PaymentMethod;
import com.aims.repository.PaymentTransactionRepository;

@Service
@RequiredArgsConstructor
public class PaymentService{
    private final PaypalService                paypalService;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public String processPayment(PaymentMethod method, int amount) {

    	if(method.equals(method)) {
    		return paypalService.placeOrder(amount);
    	} else if(method.equals(method)) {
//    		return vietqrService.placeOrder(amount);
    	}
    	
    	return "Invalid method";
    }

//    public RefundResult processRefund(String method, String orderId, long amountVND) {
//        PaymentGateway gateway = gatewayFactory.getGateway(method);
//        return gateway.processRefund(orderId, amountVND);
//    }
    
	@Transactional
    public PaymentTransaction processPaymentTransaction(Order order, int amount, String content, PaymentMethod method) {
        
        // 1. Create the base transaction data
        Transaction baseTransaction = new Transaction();
        baseTransaction.setAmount(amount);

        // 2. Create the specific payment transaction using your copy constructor
        PaymentTransaction payment = new PaymentTransaction(baseTransaction);
        payment.setOrder(order);
        payment.setTransactionContent(content);
        payment.setPaymentMethod(method);

        // 3. Save to the database
        return paymentTransactionRepository.save(payment);
    }
}
