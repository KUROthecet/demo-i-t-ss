package com.aims.service;

import com.aims.entity.Invoice;
import com.aims.entity.InvoiceDetail;
import com.aims.entity.Order;
import com.aims.entity.OrderLine;
import com.aims.exception.BusinessException;
import com.aims.repository.InvoiceRepository;
import com.aims.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, OrderRepository orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Invoice generateInvoiceFromOrder(Long orderId) {
        // 1. Fetch the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found with ID: " + orderId));

        // 2. Prevent duplicate invoices for the same order
        if (invoiceRepository.existsByOrderId(orderId)) {
            throw new BusinessException("An invoice has already been generated for this order.");
        }

        // 3. Initialize the Invoice
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        
        // Note: Casting int to double based on your entity types
        invoice.setTotalPriceExcludingVat(order.getSubtotal());
        invoice.setTotalPriceIncludingVat((double) order.getSubtotal() + order.getVat());
        invoice.setDeliveryFee((double) order.getDeliveryFee() + order.getRushFee());
        invoice.setTotalAmount(order.getTotalAmount());

        // 4. Map OrderLines to InvoiceDetails
        List<InvoiceDetail> invoiceDetails = order.getOrderLines().stream()
                .map(orderLine -> createInvoiceDetail(orderLine, invoice))
                .collect(Collectors.toList());

        // 5. Attach details to invoice
        invoice.setProductList(invoiceDetails);

        // 6. Save the Invoice (CascadeType.ALL will automatically save the details)
        return invoiceRepository.save(invoice);
    }

    /**
     * Helper method to map a single OrderLine to an InvoiceDetail
     */
    private InvoiceDetail createInvoiceDetail(OrderLine orderLine, Invoice invoice) {
        InvoiceDetail detail = new InvoiceDetail();
        
        detail.setProduct(orderLine.getMedia());
        detail.setQuantity(orderLine.getQuantity());
        detail.setPrice(orderLine.getUnitPrice());
        
        // CRITICAL: Set the parent reference to satisfy the bidirectional relationship
        detail.setInvoice(invoice); 
        
        return detail;
    }
}