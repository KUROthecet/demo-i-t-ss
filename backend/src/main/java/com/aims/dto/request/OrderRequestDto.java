package com.aims.dto.request;

import com.aims.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class OrderRequestDto {

    @NotBlank(message = "Customer name must not be blank")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Customer name must contain only letters and spaces")
    private String customerName;

    @NotBlank(message = "Customer email must not be blank")
    @Email(message = "Customer email must be a valid email address")
    private String customerEmail;

    @NotBlank(message = "Customer phone must not be blank")
    @Pattern(regexp = "^\\d+$", message = "Customer phone must contain only digits")
    private String customerPhone;

    @NotBlank(message = "Delivery address must not be blank")
    private String deliveryAddress;

    @NotBlank(message = "Province must not be blank")
    private String province;

    private String deliveryNotes;

    @NotNull(message = "Rush delivery flag must not be null")
    private Boolean rushDelivery;

    private String preferredDeliveryTime;

    @NotNull(message = "Payment method must not be null")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderLineRequestDto> orderLines;

    public OrderRequestDto() {}

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

    public Boolean getRushDelivery() { return rushDelivery; }
    public void setRushDelivery(Boolean rushDelivery) { this.rushDelivery = rushDelivery; }

    public String getPreferredDeliveryTime() { return preferredDeliveryTime; }
    public void setPreferredDeliveryTime(String preferredDeliveryTime) { this.preferredDeliveryTime = preferredDeliveryTime; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<OrderLineRequestDto> getOrderLines() { return orderLines; }
    public void setOrderLines(List<OrderLineRequestDto> orderLines) { this.orderLines = orderLines; }
}
