package com.aims.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderLineRequestDto {

    @NotNull(message = "Media ID must not be null")
    private Long mediaId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public OrderLineRequestDto() {}

    public OrderLineRequestDto(Long mediaId, int quantity) {
        this.mediaId  = mediaId;
        this.quantity = quantity;
    }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
