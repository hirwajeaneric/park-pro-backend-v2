package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DonationResponseDto {
    private UUID id;
    private UUID donorId;
    private UUID parkId;
    private BigDecimal amount;
    private String status;
    private String paymentReference;
    private String currency;
    private String motiveForDonation; // New field
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DonationResponseDto(UUID id, UUID donorId, UUID parkId, BigDecimal amount, String status,
                               String paymentReference, String currency, String motiveForDonation,
                               LocalDateTime confirmedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.donorId = donorId;
        this.parkId = parkId;
        this.amount = amount;
        this.status = status;
        this.paymentReference = paymentReference;
        this.currency = currency;
        this.motiveForDonation = motiveForDonation;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getDonorId() { return donorId; }
    public UUID getParkId() { return parkId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public String getCurrency() { return currency; }
    public String getMotiveForDonation() { return motiveForDonation; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}