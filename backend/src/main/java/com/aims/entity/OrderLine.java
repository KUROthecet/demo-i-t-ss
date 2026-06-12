package com.aims.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_line")
@Data
@NoArgsConstructor
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    private int quantity;

    private int unitPrice;

    private String titleSnapshot;

    public int getSubtotal() {
        return unitPrice * quantity;
    }

    public double getShippingWeight() {
        return media != null ? media.getShippingWeight() * quantity : 0.0;
    }
}
