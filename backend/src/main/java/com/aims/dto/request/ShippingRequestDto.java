package com.aims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShippingRequestDto {

    @NotNull(message = "Weight must not be null")
    private Double weight;

    @NotBlank(message = "Province must not be blank")
    private String province;

    private double orderTotal;

    @NotNull(message = "Rush delivery flag must not be null")
    private Boolean rushDelivery;

    public ShippingRequestDto() {}

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public double getOrderTotal() { return orderTotal; }
    public void setOrderTotal(double orderTotal) { this.orderTotal = orderTotal; }

    public Boolean getRushDelivery() { return rushDelivery; }
    public void setRushDelivery(Boolean rushDelivery) { this.rushDelivery = rushDelivery; }
}
