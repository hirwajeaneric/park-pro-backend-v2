package com.park.parkpro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateDonationRequestDto {
    @NotNull
    private UUID parkId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 500, message = "Motive for donation must be 500 characters or less")
    private String motiveForDonation; // Optional field

    // Getters and Setters
    public UUID getParkId() { return parkId; }
    public void setParkId(UUID parkId) { this.parkId = parkId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMotiveForDonation() { return motiveForDonation; }
    public void setMotiveForDonation(String motiveForDonation) { this.motiveForDonation = motiveForDonation; }
}