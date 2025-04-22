package com.park.parkpro.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OpportunityResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String details;
    private String type;
    private String status;
    private String visibility;
    private UUID createdById;
    private UUID parkId; // New field
    private String parkName; // New field
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OpportunityResponseDto(UUID id, String title, String description, String details,
                                  String type, String status, String visibility, UUID createdById,
                                  UUID parkId, String parkName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.details = details;
        this.type = type;
        this.status = status;
        this.visibility = visibility;
        this.createdById = createdById;
        this.parkId = parkId;
        this.parkName = parkName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDetails() { return details; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getVisibility() { return visibility; }
    public UUID getCreatedById() { return createdById; }
    public UUID getParkId() { return parkId; }
    public String getParkName() { return parkName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}