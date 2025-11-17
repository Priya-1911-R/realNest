package com.example.realNest.dto;

import java.math.BigDecimal;

import com.example.realNest.model.Property;

public class PropertyDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Property.PropertyType type;
    private String location;
    private String imageUrl;
    private String ownerName;

    // Constructors
    public PropertyDto() {}

    public PropertyDto(Property property) {
        this.id = property.getId();
        this.title = property.getTitle();
        this.description = property.getDescription();
        this.price = property.getPrice();
        this.type = property.getType();
        this.location = property.getLocation();
        this.imageUrl = property.getImageUrl();
        this.ownerName = property.getOwner().getName();
    }

    // Getters and setters
    // ... (for all fields)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }    

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }


    public Property.PropertyType getType() { return type; }
    public void setType(Property.PropertyType type) { this.type = type; }


    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    
}