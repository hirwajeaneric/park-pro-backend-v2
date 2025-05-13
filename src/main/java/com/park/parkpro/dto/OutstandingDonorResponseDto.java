package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class OutstandingDonorResponseDto {
    private UUID donorId;
    private String donorName;
    private BigDecimal totalDonationAmount;
    private String motiveForDonation;

    public OutstandingDonorResponseDto(UUID donorId, String donorName, BigDecimal totalDonationAmount, String motiveForDonation) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.totalDonationAmount = totalDonationAmount;
        this.motiveForDonation = motiveForDonation;
    }

    // Getters
    public UUID getDonorId() { return donorId; }
    public String getDonorName() { return donorName; }
    public BigDecimal getTotalDonationAmount() { return totalDonationAmount; }
    public String getMotiveForDonation() { return motiveForDonation; }
}