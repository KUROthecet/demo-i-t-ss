package com.aims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class PhysicalMedia extends Media {

    @Column(columnDefinition = "float8 default 0.5 not null")
    private double weight;

    private String dimensions;

    @Override
    public double getShippingWeight() {
        return this.weight;
    }

    @Override
    public void updateDetails(Media updated) {
        super.updateDetails(updated);
        if (updated instanceof PhysicalMedia other) {
            this.weight     = other.getWeight();
            this.dimensions = other.getDimensions();
        }
    }
}
