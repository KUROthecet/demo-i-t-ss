package com.aims.dto.response;

import com.aims.entity.Media;
import com.aims.entity.PhysicalMedia;

import java.util.Map;

public class MediaResponseDto {

    private Long    id;
    private String  barcode;
    private String  title;
    private String  category;
    private int     originalPrice;
    private int     currentPrice;
    private String  generalDescription;
    private String  dimensions;
    private double  weight;
    private String  imageUrl;
    private int     quantityInStock;
    private String  status;
    private boolean supportRushDelivery;

    private Map<String, String> attributes;

    public MediaResponseDto() {}

    public static MediaResponseDto fromEntity(Media media) {
        MediaResponseDto dto = new MediaResponseDto();

        dto.setId(media.getId());
        dto.setBarcode(media.getBarcode());
        dto.setTitle(media.getTitle());
        dto.setCategory(media.getCategory());
        dto.setOriginalPrice(media.getOriginalPrice());
        dto.setCurrentPrice(media.getCurrentPrice());
        dto.setGeneralDescription(media.getGeneralDescription());
        dto.setImageUrl(media.getImageUrl());
        dto.setQuantityInStock(media.getQuantityInStock());
        dto.setStatus(media.getStatus() != null ? media.getStatus().name() : null);
        dto.setSupportRushDelivery(media.isSupportRushDelivery());

        if (media instanceof PhysicalMedia physical) {
            dto.setWeight(physical.getWeight());
            dto.setDimensions(physical.getDimensions());
        }

        dto.setAttributes(media.getTypeSpecificAttributes());

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(int originalPrice) { this.originalPrice = originalPrice; }

    public int getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }

    public String getGeneralDescription() { return generalDescription; }
    public void setGeneralDescription(String generalDescription) { this.generalDescription = generalDescription; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSupportRushDelivery() { return supportRushDelivery; }
    public void setSupportRushDelivery(boolean supportRushDelivery) { this.supportRushDelivery = supportRushDelivery; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
}
