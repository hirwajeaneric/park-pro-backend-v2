package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ActivityResponseDto {
    private UUID id;
    private String name;
    private UUID parkId;
    private BigDecimal price;
    private String description;
    private String picture;
    private Integer capacityPerDay; // New field
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ActivityResponseDto(UUID id, String name, UUID parkId, BigDecimal price, String description, String picture, Integer capacityPerDay, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.parkId = parkId;
        this.price = price;
        this.description = description;
        this.picture = picture;
        this.capacityPerDay = capacityPerDay;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getParkId() { return parkId; }
    public BigDecimal getPrice() { return price; }
    public String getDescription() { return description; }
    public String getPicture() { return picture; }
    public Integer getCapacityPerDay() { return capacityPerDay; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}