package com.park.parkpro.dto;

import com.park.parkpro.domain.AuditStatus;
import com.park.parkpro.domain.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExpenseResponseDto {
    private UUID id;
    private UUID budgetId;
    private BigDecimal amount;
    private String description;
    private UUID budgetCategoryId;
    private String budgetCategoryName;
    private UUID parkId;
    private UUID createdBy;
    private AuditStatus auditStatus;
    private String receiptUrl;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseResponseDto(UUID id, UUID budgetId, BigDecimal amount, String description,
                              UUID budgetCategoryId, String budgetCategoryName, UUID parkId,
                              UUID createdBy, AuditStatus auditStatus, String receiptUrl,
                              String currency, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.budgetId = budgetId;
        this.amount = amount;
        this.description = description;
        this.budgetCategoryId = budgetCategoryId;
        this.budgetCategoryName = budgetCategoryName;
        this.parkId = parkId;
        this.createdBy = createdBy;
        this.auditStatus = auditStatus;
        this.receiptUrl = receiptUrl;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getBudgetId() { return budgetId; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public UUID getBudgetCategoryId() { return budgetCategoryId; }
    public String getBudgetCategoryName() { return budgetCategoryName; }
    public UUID getParkId() { return parkId; }
    public UUID getCreatedBy() { return createdBy; }
    public AuditStatus getAuditStatus() { return auditStatus; }
    public String getReceiptUrl() { return receiptUrl; }
    public String getCurrency() { return currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}