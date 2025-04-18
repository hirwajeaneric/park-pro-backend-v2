package com.park.parkpro.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_category_id", nullable = false)
    private BudgetCategory budgetCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", nullable = false)
    private AuditStatus auditStatus = AuditStatus.UNJUSTIFIED;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "currency", nullable = false)
    private String currency = "XAF";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum AuditStatus {
        PASSED, FAILED, UNJUSTIFIED
    }

    // Constructors
    public Expense() {}

    public Expense(Budget budget, BigDecimal amount, String description, BudgetCategory budgetCategory,
                   Park park, User createdBy, AuditStatus auditStatus) {
        this.budget = budget;
        this.amount = amount;
        this.description = description;
        this.budgetCategory = budgetCategory;
        this.park = park;
        this.createdBy = createdBy;
        this.auditStatus = auditStatus != null ? auditStatus : AuditStatus.UNJUSTIFIED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Budget getBudget() { return budget; }
    public void setBudget(Budget budget) { this.budget = budget; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BudgetCategory getBudgetCategory() { return budgetCategory; }
    public void setBudgetCategory(BudgetCategory budgetCategory) { this.budgetCategory = budgetCategory; }
    public Park getPark() { return park; }
    public void setPark(Park park) { this.park = park; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}