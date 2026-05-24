package com.aims.dto.response;

import com.aims.entity.Order;
import com.aims.entity.OrderLine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDto {

    private Long          id;
    private String        orderCode;
    private String        customerName;
    private String        customerEmail;
    private String        customerPhone;
    private String        deliveryAddress;
    private String        province;
    private String        deliveryNotes;
    private boolean       rushDelivery;
    private String        preferredDeliveryTime;
    private int           subtotal;
    private int           vat;
    private int           deliveryFee;
    private int           rushFee;
    private int           totalAmount;
    private String        status;
    private String        rejectionReason;
    private String        paymentMethod;
    private String        paymentTransactionId;
    private String        paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime lastUpdated;
    private List<OrderLineResponseDto> orderLines;

    public OrderResponseDto() {}

    public static OrderResponseDto fromEntity(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderCode(order.getOrderCode());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setProvince(order.getProvince());
        dto.setDeliveryNotes(order.getDeliveryNotes());
        dto.setRushDelivery(order.isRushDelivery());
        dto.setPreferredDeliveryTime(order.getPreferredDeliveryTime());
        dto.setSubtotal(order.getSubtotal());
        dto.setVat(order.getVat());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setRushFee(order.getRushFee());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setRejectionReason(order.getRejectionReason());
        dto.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        dto.setPaymentTransactionId(order.getPaymentTransactionId());
        dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        dto.setOrderDate(order.getOrderDate());
        dto.setLastUpdated(order.getLastUpdated());

        if (order.getOrderLines() != null) {
            dto.setOrderLines(order.getOrderLines().stream()
                    .map(OrderLineResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public static class OrderLineResponseDto {
        private Long   id;
        private Long   mediaId;
        private String titleSnapshot;
        private int    quantity;
        private int    unitPrice;

        public OrderLineResponseDto() {}

        public static OrderLineResponseDto fromEntity(OrderLine line) {
            OrderLineResponseDto dto = new OrderLineResponseDto();
            dto.setId(line.getId());
            dto.setMediaId(line.getMedia() != null ? line.getMedia().getId() : null);
            dto.setTitleSnapshot(line.getTitleSnapshot());
            dto.setQuantity(line.getQuantity());
            dto.setUnitPrice(line.getUnitPrice());
            return dto;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getMediaId() { return mediaId; }
        public void setMediaId(Long mediaId) { this.mediaId = mediaId; }
        public String getTitleSnapshot() { return titleSnapshot; }
        public void setTitleSnapshot(String titleSnapshot) { this.titleSnapshot = titleSnapshot; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getUnitPrice() { return unitPrice; }
        public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }

    public boolean isRushDelivery() { return rushDelivery; }
    public void setRushDelivery(boolean rushDelivery) { this.rushDelivery = rushDelivery; }

    public String getPreferredDeliveryTime() { return preferredDeliveryTime; }
    public void setPreferredDeliveryTime(String preferredDeliveryTime) { this.preferredDeliveryTime = preferredDeliveryTime; }

    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }

    public int getVat() { return vat; }
    public void setVat(int vat) { this.vat = vat; }

    public int getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(int deliveryFee) { this.deliveryFee = deliveryFee; }

    public int getRushFee() { return rushFee; }
    public void setRushFee(int rushFee) { this.rushFee = rushFee; }

    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<OrderLineResponseDto> getOrderLines() { return orderLines; }
    public void setOrderLines(List<OrderLineResponseDto> orderLines) { this.orderLines = orderLines; }
}
