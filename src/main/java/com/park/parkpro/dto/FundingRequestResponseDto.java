package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class FundingRequestResponseDto {
    private UUID id;
    private UUID parkId;
    private String parkName;
    private UUID budgetId;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private String requestType;
    private String reason;
    private UUID requesterId;
    private UUID approverId;
    private String status;
    private String rejectionReason;
    private LocalDateTime approvedAt;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FundingRequestResponseDto(UUID id, UUID parkId, String parkName, UUID budgetId, BigDecimal requestedAmount,
                                     BigDecimal approvedAmount, String requestType, String reason,
                                     UUID requesterId, UUID approverId, String status, String rejectionReason,
                                     LocalDateTime approvedAt, String currency, LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
        this.id = id;
        this.parkId = parkId;
        this.parkName = parkName;
        this.budgetId = budgetId;
        this.requestedAmount = requestedAmount;
        this.approvedAmount = approvedAmount;
        this.requestType = requestType;
        this.reason = reason;
        this.requesterId = requesterId;
        this.approverId = approverId;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.approvedAt = approvedAt;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getParkId() { return parkId; }
    public String getParkName() { return parkName; }
    public UUID getBudgetId() { return budgetId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public String getRequestType() { return requestType; }
    public String getReason() { return reason; }
    public UUID getRequesterId() { return requesterId; }
    public UUID getApproverId() { return approverId; }
    public String getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}