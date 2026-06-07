package com.aims.service;

import com.aims.entity.Invoice;
import com.aims.entity.InvoiceDetail;
import com.aims.entity.Order;
import com.aims.entity.OrderLine;
import com.aims.exception.BusinessException;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository   orderRepository;

    public Invoice generateInvoiceFromOrder(Long orderId) {
        Optional<Order> foundOrder = orderRepository.findById(orderId);
        if (foundOrder.isEmpty()) {
            throw new BusinessException("Order not found with ID: " + orderId);
        }
        Order order = foundOrder.get();

        if (invoiceRepository.existsByOrderId(orderId)) {
            throw new BusinessException("An invoice has already been generated for this order.");
        }

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setTotalPriceExcludingVat(order.getSubtotal());
        invoice.setTotalPriceIncludingVat((double) order.getSubtotal() + order.getVat());
        invoice.setDeliveryFee((double) order.getDeliveryFee() + order.getRushFee());
        invoice.setTotalAmount(order.getTotalAmount());

        List<InvoiceDetail> invoiceDetails = new ArrayList<>();
        for (OrderLine orderLine : order.getOrderLines()) {
            invoiceDetails.add(buildInvoiceDetail(orderLine, invoice));
        }

        invoice.setProductList(invoiceDetails);
        return invoiceRepository.save(invoice);
    }

    private InvoiceDetail buildInvoiceDetail(OrderLine orderLine, Invoice invoice) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setProduct(orderLine.getMedia());
        detail.setQuantity(orderLine.getQuantity());
        detail.setPrice(orderLine.getUnitPrice());
        detail.setInvoice(invoice);
        return detail;
    }
}
