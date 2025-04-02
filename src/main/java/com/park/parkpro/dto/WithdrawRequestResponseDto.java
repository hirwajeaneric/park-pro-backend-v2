package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WithdrawRequestResponseDto {
    private UUID id;
    private BigDecimal amount;
    private String reason;
    private String description;
    private UUID requesterId;
    private UUID approverId;
    private UUID budgetCategoryId;
    private String status;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private UUID parkId;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WithdrawRequestResponseDto(UUID id, BigDecimal amount, String reason, String description,
                                      UUID requesterId, UUID approverId, UUID budgetCategoryId, String status,
                                      LocalDateTime approvedAt, String rejectionReason, UUID parkId, String currency,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.amount = amount;
        this.reason = reason;
        this.description = description;
        this.requesterId = requesterId;
        this.approverId = approverId;
        this.budgetCategoryId = budgetCategoryId;
        this.status = status;
        this.approvedAt = approvedAt;
        this.rejectionReason = rejectionReason;
        this.parkId = parkId;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getDescription() { return description; }
    public UUID getRequesterId() { return requesterId; }
    public UUID getApproverId() { return approverId; }
    public UUID getBudgetCategoryId() { return budgetCategoryId; }
    public String getStatus() { return status; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public UUID getParkId() { return parkId; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}