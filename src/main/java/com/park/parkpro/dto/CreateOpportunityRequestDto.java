// src/main/java/com/park/parkpro/dto/CreateOpportunityRequestDto.java
package com.park.parkpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateOpportunityRequestDto {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String description;

    private String details;

    @NotBlank
    @Size(max = 50)
    private String type; // JOB, VOLUNTEER, PARTNERSHIP

    @NotBlank
    @Size(max = 20)
    private String status; // OPEN, CLOSED

    @NotBlank
    @Size(max = 20)
    private String visibility; // PUBLIC, PRIVATE

    @NotNull
    private UUID parkId; // New field

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public UUID getParkId() { return parkId; }
    public void setParkId(UUID parkId) { this.parkId = parkId; }
}