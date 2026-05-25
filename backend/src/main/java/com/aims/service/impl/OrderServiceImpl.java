/*
Coupling level: Content Coupling 
Reason why: Service acts as an overarching class, bypassing encapsulation to directly set the entities' internal state.
*/

/*
Cohesion Level: Procedural Cohesion
Reason Why: 
createOrder follows a sequential pipeline where inventory checking, stock reduction, fee calculations, 
payment processing, database saving and email delivery depend directly on the output of the preceding step
*/

package com.aims.service.impl;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aims.adapter.RefundResult;
import com.aims.dto.request.OrderLineRequestDto;
import com.aims.dto.request.OrderRequestDto;
import com.aims.dto.request.PaymentRequestDto;
import com.aims.dto.response.PaymentResponseDto;
import com.aims.entity.HistoryLog;
import com.aims.entity.Media;
import com.aims.entity.Order;
import com.aims.entity.OrderLine;
import com.aims.enums.OrderStatus;
import com.aims.enums.PaymentMethod;
import com.aims.enums.PaymentStatus;
import com.aims.exception.BusinessException;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.HistoryLogRepository;
import com.aims.repository.MediaRepository;
import com.aims.repository.OrderRepository;
import com.aims.service.EmailService;
import com.aims.service.OrderService;
import com.aims.service.PaymentService;
import com.aims.service.ShippingCalculatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository           orderRepository;
    private final MediaRepository           mediaRepository;
    private final HistoryLogRepository      historyLogRepository;
    private final EmailService              emailService;
    private final PaymentService            paymentService;
    private final ShippingCalculatorService shippingCalculatorService;

    private static final double VAT_RATE      = 0.10;
    private static final String MANAGER_EMAIL = "manager@aims.vn";

    @Override
    public Order createOrder(OrderRequestDto dto) {
        log.info("Creating order for customer: {}", dto.getCustomerEmail());

        Order order      = new Order();
        List<OrderLine> orderLines = new ArrayList<>();
        int    subtotal     = 0;
        double totalWeight  = 0.0;

        for (OrderLineRequestDto lineDto : dto.getOrderLines()) {
            Media media = mediaRepository.findById(lineDto.getMediaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Media not found with ID: " + lineDto.getMediaId()));

            if (!media.isAvailable()) {
                throw new BusinessException("Media '" + media.getTitle() + "' is not available for purchase.");
            }

            media.reduceStock(lineDto.getQuantity());
            mediaRepository.save(media);

            OrderLine line = new OrderLine();
            line.setMedia(media);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitPrice(media.getCurrentPrice());
            line.setTitleSnapshot(media.getTitle());
            line.setOrder(order);
            orderLines.add(line);

            subtotal    += media.getCurrentPrice() * lineDto.getQuantity();
            totalWeight += media.getWeight()       * lineDto.getQuantity();
        }

        int    vat         = (int) Math.round(subtotal * VAT_RATE);
        double shippingFee = shippingCalculatorService.calculateFee(
                totalWeight, dto.getProvince(), subtotal, Boolean.TRUE.equals(dto.getRushDelivery()));
        int deliveryFee;
        int rushFee = 0;

        if (Boolean.TRUE.equals(dto.getRushDelivery())) {
            double standardFee = shippingCalculatorService.calculateFee(
                    totalWeight, dto.getProvince(), subtotal, false);
            deliveryFee = (int) Math.round(standardFee);
            rushFee     = (int) Math.round(shippingFee - standardFee);
        } else {
            deliveryFee = (int) Math.round(shippingFee);
        }

        int total = subtotal + vat + deliveryFee + rushFee;

        order.setCustomerName(dto.getCustomerName());
        order.setCustomerEmail(dto.getCustomerEmail());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setProvince(dto.getProvince());
        order.setDeliveryNotes(dto.getDeliveryNotes());
        order.setRushDelivery(Boolean.TRUE.equals(dto.getRushDelivery()));
        order.setPreferredDeliveryTime(dto.getPreferredDeliveryTime());
        order.setSubtotal(subtotal);
        order.setVat(vat);
        order.setDeliveryFee(deliveryFee);
        order.setRushFee(rushFee);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING_PROCESSING);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderLines(orderLines);
        order.setOrderCode("ORD-" + Year.now().getValue() + "-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());

        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setAmount(BigDecimal.valueOf(total)); // Assuming total is a double
        paymentRequest.setPaymentMethod(dto.getPaymentMethod().name());
        paymentRequest.setOrderInfo("AIMS Order Payment - " + order.getOrderCode());

        PaymentResponseDto paymentResult = paymentService.processPayment(paymentRequest);

        if (paymentResult.getMessage() == "Failed") {
            throw new BusinessException("Payment failed: " + paymentResult.getMessage());
        }

        order.markAsPaid();
        Order savedOrder = orderRepository.save(order);

        emailService.sendOrderConfirmation(dto.getCustomerEmail(), dto.getCustomerName(), order.getOrderCode(), total);
        logOrderAction("ORDER_CREATED", savedOrder.getId().toString(), "SYSTEM",
                "Order " + order.getOrderCode() + " created for " + dto.getCustomerEmail() +
                " | Total: " + total + " VND");

        log.info("Order created successfully: {}, total: {} VND", order.getOrderCode(), total);
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByCode(String code) {
        return orderRepository.findByOrderCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatusOrderByOrderDateDesc(OrderStatus.PENDING_PROCESSING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByOrderDateDesc(email);
    }

    @Override
    public Order approveOrder(Long id, String performedBy) {
        log.info("Approving order ID: {}, by: {}", id, performedBy);
        Order order = getOrderById(id);
        order.approve();
        Order saved = orderRepository.save(order);

        emailService.sendOrderApproved(order.getCustomerEmail(), order.getCustomerName(), order.getOrderCode());
        logOrderAction("ORDER_APPROVED", order.getOrderCode(), performedBy,
                "Order " + order.getOrderCode() + " approved");

        log.info("Order {} approved", order.getOrderCode());
        return saved;
    }

    @Override
    public Order rejectOrder(Long id, String reason, String performedBy) {
        log.info("Rejecting order ID: {}, reason: {}, by: {}", id, reason, performedBy);
        Order order = getOrderById(id);
        order.reject(reason);
        restoreInventory(order);
        processOrderRefund(order);
        Order saved = orderRepository.save(order);

        emailService.sendOrderRejected(order.getCustomerEmail(), order.getCustomerName(), order.getOrderCode(), reason);
        logOrderAction("ORDER_REJECTED", order.getOrderCode(), performedBy,
                "Order " + order.getOrderCode() + " rejected. Reason: " + reason);

        log.info("Order {} rejected", order.getOrderCode());
        return saved;
    }

    @Override
    public Order cancelOrder(Long id) {
        log.info("Cancelling order ID: {}", id);
        Order order = getOrderById(id);
        order.cancel();
        restoreInventory(order);
        boolean refundIssued = processOrderRefund(order);
        Order saved = orderRepository.save(order);

        emailService.sendOrderCancelled(
                order.getCustomerEmail(), order.getCustomerName(), order.getOrderCode(), refundIssued);
        logOrderAction("ORDER_CANCELLED", order.getOrderCode(), "CUSTOMER",
                "Order " + order.getOrderCode() + " cancelled");

        log.info("Order {} cancelled", order.getOrderCode());
        return saved;
    }

    private boolean processOrderRefund(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.PAYPAL) {
            RefundResult result = paymentService.processRefund(
                    order.getPaymentMethod().name(), order.getOrderCode(), order.getTotalAmount());
            if (result.success()) {
                order.markAsRefunded();
                log.info("Refund processed for order {}: {}", order.getOrderCode(), result.refundId());
                return true;
            }
        } else if (order.getPaymentMethod() == PaymentMethod.VIETQR) {
            emailService.sendManagerRefundNotification(
                    MANAGER_EMAIL, order.getOrderCode(), order.getTotalAmount(), order.getCustomerName());
        }
        return false;
    }

    private void restoreInventory(Order order) {
        for (OrderLine line : order.getOrderLines()) {
            if (line.getMedia() != null) {
                Media media = mediaRepository.findById(line.getMedia().getId()).orElse(null);
                if (media != null) {
                    media.restoreStock(line.getQuantity());
                    mediaRepository.save(media);
                    log.debug("Restored {} units to media ID: {}", line.getQuantity(), media.getId());
                }
            }
        }
    }

    private void logOrderAction(String actionType, String reference, String performedBy, String details) {
        HistoryLog entry = new HistoryLog();
        entry.setActionType(actionType);
        entry.setProductBarcode(reference);
        entry.setPerformedBy(performedBy);
        entry.setDetails(details);
        historyLogRepository.save(entry);
    }
}
