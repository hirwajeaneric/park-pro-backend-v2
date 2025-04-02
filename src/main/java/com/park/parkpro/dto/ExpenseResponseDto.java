package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExpenseResponseDto {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private String category;
    private UUID budgetCategoryId;
    private UUID parkId;
    private UUID createdBy;
    private String status;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private String receiptUrl;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseResponseDto(UUID id, BigDecimal amount, String description, String category, UUID budgetCategoryId,
                              UUID parkId, UUID createdBy, String status, UUID approvedBy, LocalDateTime approvedAt,
                              String receiptUrl, String currency, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.budgetCategoryId = budgetCategoryId;
        this.parkId = parkId;
        this.createdBy = createdBy;
        this.status = status;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public UUID getBudgetCategoryId() { return budgetCategoryId; }
    public UUID getParkId() { return parkId; }
    public UUID getCreatedBy() { return createdBy; }
    public String getStatus() { return status; }
    public UUID getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getReceiptUrl() { return receiptUrl; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}