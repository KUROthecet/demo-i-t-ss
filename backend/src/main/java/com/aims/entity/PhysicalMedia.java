package com.aims.entity;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
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

    @Column(columnDefinition = "float8 default 0 not null")
    private double heightCm;

    @Column(columnDefinition = "float8 default 0 not null")
    private double widthCm;

    @Column(columnDefinition = "float8 default 0 not null")
    private double lengthCm;

    @Override
    public double getShippingWeight() {
        return this.weight;
    }

    public String getDimensions() {
        if (heightCm <= 0 && widthCm <= 0 && lengthCm <= 0) return null;
        return fmt(heightCm) + "×" + fmt(widthCm) + "×" + fmt(lengthCm) + " cm";
    }

    @JsonSetter("dimensions")
    public void parseDimensions(String raw) {
        if (raw == null || raw.isBlank()) return;
        String[] parts = raw.toLowerCase().replace("cm", "").trim().split("[x×]");
        if (parts.length >= 1) this.heightCm = parseDouble(parts[0]);
        if (parts.length >= 2) this.widthCm  = parseDouble(parts[1]);
        if (parts.length >= 3) this.lengthCm = parseDouble(parts[2]);
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    private static String fmt(double n) {
        return n == Math.floor(n) ? String.valueOf((long) n) : String.valueOf(n);
    }

    @Override
    public void updateDetails(Media updated) {
        super.updateDetails(updated);
        if (updated instanceof PhysicalMedia other) {
            this.weight   = other.getWeight();
            this.heightCm = other.getHeightCm();
            this.widthCm  = other.getWidthCm();
            this.lengthCm = other.getLengthCm();
        }
    }
}
