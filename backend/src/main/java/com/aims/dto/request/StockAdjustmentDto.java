package com.aims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentDto {

    @NotNull(message = "Media ID must not be null")
    private Long mediaId;

    // Positive = add stock, Negative = remove stock
    private int quantityDelta;

    @NotBlank(message = "Reason must not be blank")
    private String reason;

    private String performedBy;

    public StockAdjustmentDto() {}

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public int getQuantityDelta() { return quantityDelta; }
    public void setQuantityDelta(int quantityDelta) { this.quantityDelta = quantityDelta; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
