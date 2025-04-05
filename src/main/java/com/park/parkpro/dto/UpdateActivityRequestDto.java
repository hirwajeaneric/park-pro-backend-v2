package com.park.parkpro.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class UpdateActivityRequestDto {
    private String name;
    @PositiveOrZero
    private BigDecimal price;
    private String description;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}