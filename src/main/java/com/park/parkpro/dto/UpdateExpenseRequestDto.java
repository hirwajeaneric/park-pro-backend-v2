package com.park.parkpro.dto;

import com.park.parkpro.domain.Expense;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateExpenseRequestDto {
    private BigDecimal amount;
    private String description;
    private UUID budgetCategoryId;
    private String receiptUrl;
    private Expense.AuditStatus auditStatus;

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getBudgetCategoryId() { return budgetCategoryId; }
    public void setBudgetCategoryId(UUID budgetCategoryId) { this.budgetCategoryId = budgetCategoryId; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public Expense.AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(Expense.AuditStatus auditStatus) { this.auditStatus = auditStatus; }
}