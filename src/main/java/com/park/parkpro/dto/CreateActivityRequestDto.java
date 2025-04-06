package com.park.parkpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class CreateActivityRequestDto {
    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    private String description;

    private String picture;

    @PositiveOrZero
    private Integer capacityPerDay; // New field

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public String getPicture() { return picture; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCapacityPerDay() { return capacityPerDay; }
    public void setCapacityPerDay(Integer capacityPerDay) { this.capacityPerDay = capacityPerDay; }
}