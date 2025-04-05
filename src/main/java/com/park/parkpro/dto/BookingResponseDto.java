package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BookingResponseDto {
    private UUID id;
    private UUID visitorId;
    private UUID activityId;
    private BigDecimal amount;
    private UUID parkId;
    private LocalDate visitDate;
    private String status;
    private String paymentReference;
    private String currency;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingResponseDto(UUID id, UUID visitorId, UUID activityId, BigDecimal amount, UUID parkId,
                              LocalDate visitDate, String status, String paymentReference, String currency,
                              LocalDateTime confirmedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.visitorId = visitorId;
        this.activityId = activityId;
        this.amount = amount;
        this.parkId = parkId;
        this.visitDate = visitDate;
        this.status = status;
        this.paymentReference = paymentReference;
        this.currency = currency;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getVisitorId() { return visitorId; }
    public UUID getActivityId() { return activityId; }
    public BigDecimal getAmount() { return amount; }
    public UUID getParkId() { return parkId; }
    public LocalDate getVisitDate() { return visitDate; }
    public String getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public String getCurrency() { return currency; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}