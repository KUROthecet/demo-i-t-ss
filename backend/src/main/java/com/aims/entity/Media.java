/*
Coupling level: Content coupling
Reason why: Entities expose all internal state via @Data; service uses blind bulk-setters (updateDetails) to copy fields.
*/

package com.aims.entity;

import com.aims.enums.MediaStatus;
import com.aims.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(
    use      = JsonTypeInfo.Id.NAME,
    include  = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "category",
    visible  = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Book.class,      name = "Book"),
    @JsonSubTypes.Type(value = CD.class,        name = "CD"),
    @JsonSubTypes.Type(value = DVD.class,       name = "DVD"),
    @JsonSubTypes.Type(value = Newspaper.class, name = "Newspaper")
})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "media")
@Data
@NoArgsConstructor
public abstract class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Shared fields — Table 10 from AIMS SRS */

    @NotBlank(message = "Barcode is required")
    @Column(unique = true, nullable = false)
    private String barcode;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull
    @Positive(message = "Original price must be positive")
    private int originalPrice;

    @NotNull
    @Positive(message = "Current price must be positive")
    private int currentPrice;

    @Column(columnDefinition = "TEXT")
    private String generalDescription;

    private String dimensions;

    @NotNull
    @Positive(message = "Weight must be positive")
    private double weight;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    private int quantityInStock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status = MediaStatus.ACTIVE;

    private boolean supportRushDelivery;

    // UC003/UC004: price must be 30%–150% of original
    public void updatePrice(int newPrice) {
        double minPrice = originalPrice * 0.30;
        double maxPrice = originalPrice * 1.50;
        if (newPrice < minPrice || newPrice > maxPrice) {
            throw new BusinessException(
                String.format(
                    "Price %,d VND is out of allowed range [%,.0f VND – %,.0f VND] " +
                    "(30%%-150%% of original price %,d VND).",
                    newPrice, minPrice, maxPrice, originalPrice
                )
            );
        }
        this.currentPrice = newPrice;
    }

    // UC007: reduce stock on order placement; deactivate when stock hits zero
    public void reduceStock(int quantity) {
        if (this.quantityInStock < quantity) {
            throw new BusinessException(
                String.format(
                    "Insufficient stock for '%s'. Available: %d, Requested: %d.",
                    this.title, this.quantityInStock, quantity
                )
            );
        }
        this.quantityInStock -= quantity;
        if (this.quantityInStock == 0) this.status = MediaStatus.DEACTIVATED;
    }

    // UC011: restore stock after cancellation/rejection; reactivate if was deactivated
    public void restoreStock(int quantity) {
        this.quantityInStock += quantity;
        if (MediaStatus.DEACTIVATED == this.status && this.quantityInStock > 0) {
            this.status = MediaStatus.ACTIVE;
        }
    }

    public void updateDetails(Media updated) {
        this.barcode             = updated.getBarcode();
        this.title               = updated.getTitle();
        this.category            = updated.getCategory();
        this.originalPrice       = updated.getOriginalPrice();
        this.generalDescription  = updated.getGeneralDescription();
        this.dimensions          = updated.getDimensions();
        this.weight              = updated.getWeight();
        this.imageUrl            = updated.getImageUrl();
        this.quantityInStock     = updated.getQuantityInStock();
        this.status              = updated.getStatus();
        this.supportRushDelivery = updated.isSupportRushDelivery();
        this.updatePrice(updated.getCurrentPrice());
    }

    // UC005: soft-delete when stock > 0
    public void deactivate() {
        this.status = MediaStatus.DEACTIVATED;
    }

    public boolean isAvailable() {
        return MediaStatus.ACTIVE == this.status && this.quantityInStock > 0;
    }

    public boolean canBeDeleted() {
        return this.quantityInStock == 0;
    }
}
