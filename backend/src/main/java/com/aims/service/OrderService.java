package com.aims.service;

import com.aims.config.BusinessConstants;
import com.aims.dto.request.OrderLineRequestDto;
import com.aims.dto.request.OrderRequestDto;
import com.aims.dto.response.OrderResponseDto;
import com.aims.entity.*;
import com.aims.enums.OrderStatus;
import com.aims.enums.PaymentMethod;
import com.aims.enums.PaymentStatus;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository           orderRepository;
    private final MediaService              mediaService;
    private final OrderNotificationService  notificationService;
    private final PaymentService            paymentService;
    private final InvoiceService            invoiceService;
    private final ShippingCalculatorService shippingCalculatorService;
    private final HistoryLogService         historyLogService;


    @Value("${app.manager.email}")
    private String managerEmail;

    public OrderResponseDto createOrder(OrderRequestDto dto) {
        List<OrderLine> orderLines = buildOrderLines(dto);

        int    subtotal    = computeSubtotal(orderLines);
        double totalWeight = computeTotalWeight(orderLines);

        int vat        = computeVat(subtotal);
        int deliveryFee;
        int rushFee = 0;

        if (Boolean.TRUE.equals(dto.getRushDelivery())) {
            double standardFee = shippingCalculatorService.calculateStandardFee(totalWeight, dto.getProvince(), subtotal);
            double rushTotal   = shippingCalculatorService.calculateRushFee(totalWeight, dto.getProvince(), subtotal);
            deliveryFee = (int) Math.round(standardFee);
            rushFee     = (int) Math.round(rushTotal - standardFee);
        } else {
            deliveryFee = (int) Math.round(
                    shippingCalculatorService.calculateStandardFee(totalWeight, dto.getProvince(), subtotal));
        }

        int total = subtotal + vat + deliveryFee + rushFee;

        Order order = assembleOrder(dto, orderLines, subtotal, vat, deliveryFee, rushFee, total);
        Order savedOrder = orderRepository.save(order);

        String paymentTransactionId = paymentService.processPayment(savedOrder.getPaymentMethod(), total);
        if (paymentTransactionId != null && !paymentTransactionId.isEmpty()) {
            savedOrder.setPaymentTransactionId(paymentTransactionId);
            savedOrder = orderRepository.save(savedOrder);
        }

        paymentService.processPaymentTransaction(savedOrder, total, savedOrder.getDeliveryNotes(), savedOrder.getPaymentMethod());

        if (savedOrder.getPaymentMethod() != PaymentMethod.PAYPAL) {
            invoiceService.generateInvoiceFromOrder(savedOrder.getId());
            notificationService.sendOrderConfirmation(dto.getCustomerEmail(), dto.getCustomerName(),
                    order.getOrderCode(), total, savedOrder.getPaymentTransactionId());
        }

        historyLogService.log("ORDER_CREATED", savedOrder.getId().toString(), "SYSTEM",
                "Order " + order.getOrderCode() + " created for " + dto.getCustomerEmail() +
                " | Total: " + total + " VND");

        return OrderResponseDto.fromEntity(savedOrder);
    }

    private List<OrderLine> buildOrderLines(OrderRequestDto dto) {
        List<OrderLine> lines = new ArrayList<>();
        for (OrderLineRequestDto lineDto : dto.getOrderLines()) {
            Media media = mediaService.validateAndDeductStock(lineDto.getMediaId(), lineDto.getQuantity());
            OrderLine line = new OrderLine();
            line.setMedia(media);
            line.setQuantity(lineDto.getQuantity());
            line.setUnitPrice(media.getCurrentPrice());
            line.setTitleSnapshot(media.getTitle());
            lines.add(line);
        }
        return lines;
    }

    private int computeSubtotal(List<OrderLine> lines) {
        return lines.stream().mapToInt(OrderLine::getSubtotal).sum();
    }

    private double computeTotalWeight(List<OrderLine> lines) {
        return lines.stream().mapToDouble(OrderLine::getShippingWeight).sum();
    }

    private int computeVat(int subtotal) {
        return (int) Math.round(subtotal * BusinessConstants.VAT_RATE);
    }

    private Order assembleOrder(OrderRequestDto dto, List<OrderLine> orderLines,
                                int subtotal, int vat, int deliveryFee, int rushFee, int total) {
        Order order = new Order();
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
        order.setOrderCode(generateOrderCode());
        for (OrderLine line : orderLines) {
            line.setOrder(order);
        }
        order.setOrderLines(orderLines);
        return order;
    }

    private String generateOrderCode() {
        return "ORD-" + Year.now().getValue() + "-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        Optional<Order> found = orderRepository.findById(id);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Order not found with ID: " + id);
        }
        return found.get();
    }

    @Transactional(readOnly = true)
    public Order getOrderByCode(String code) {
        Optional<Order> found = orderRepository.findByOrderCode(code);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Order not found with code: " + code);
        }
        return found.get();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> getPendingOrders(int page, int size) {
        return orderRepository.findByStatusOrderByOrderDateDesc(
                OrderStatus.PENDING_PROCESSING, org.springframework.data.domain.PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> getAllOrders(int page, int size) {
        return orderRepository.findAllByOrderByOrderDateDesc(org.springframework.data.domain.PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        return orderRepository.findByStatusOrderByOrderDateDesc(
                status, org.springframework.data.domain.PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByOrderDateDesc(email);
    }

    public Order approveOrder(Long id, String performedBy) {
        Order order = getOrderById(id);
        order.approve();
        Order saved = orderRepository.save(order);
        notificationService.sendOrderApproved(order.getCustomerEmail(), order.getCustomerName(), order.getOrderCode());
        historyLogService.log("ORDER_APPROVED", order.getOrderCode(), performedBy,
                "Order " + order.getOrderCode() + " approved");
        return saved;
    }

    public Order rejectOrder(Long id, String reason, String performedBy) {
        Order order = getOrderById(id);
        order.reject(reason);
        restoreInventory(order);
        paymentService.processRefund(order, managerEmail);
        Order saved = orderRepository.save(order);
        notificationService.sendOrderRejected(order.getCustomerEmail(), order.getCustomerName(), order.getOrderCode(), reason);
        historyLogService.log("ORDER_REJECTED", order.getOrderCode(), performedBy,
                "Order " + order.getOrderCode() + " rejected. Reason: " + reason);
        return saved;
    }

    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        order.cancel();
        restoreInventory(order);
        paymentService.processRefund(order, managerEmail);
        Order saved = orderRepository.save(order);
        notificationService.sendOrderCancelled(
                order.getCustomerEmail(), order.getCustomerName(), order.getOrderCode(),
                order.getPaymentStatus() == com.aims.enums.PaymentStatus.REFUNDED);
        historyLogService.log("ORDER_CANCELLED", order.getOrderCode(), "CUSTOMER",
                "Order " + order.getOrderCode() + " cancelled");
        return saved;
    }

    public void markOrderPaidByPaypalId(String paypalOrderId, String captureId) {
        Optional<Order> found = orderRepository.findByPaymentTransactionId(paypalOrderId);
        if (found.isEmpty()) return;

        Order order = found.get();
        if (order.getPaymentStatus() == PaymentStatus.PAID) return;

        order.markAsPaid(paypalOrderId, captureId);
        orderRepository.save(order);
        invoiceService.generateInvoiceFromOrder(order.getId());
        String paidAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        notificationService.sendPaymentConfirmation(
                order.getCustomerEmail(), order.getCustomerName(),
                order.getOrderCode(), order.getTotalAmount(),
                paypalOrderId, captureId, paidAt);
        historyLogService.log("PAYPAL_CAPTURED", order.getOrderCode(), "SYSTEM",
                "PayPal capture " + captureId + " confirmed for order " + order.getOrderCode());
    }

    private void restoreInventory(Order order) {
        for (OrderLine line : order.getOrderLines()) {
            if (line.getMedia() != null) {
                mediaService.restoreStock(line.getMedia().getId(), line.getQuantity());
            }
        }
    }
}
