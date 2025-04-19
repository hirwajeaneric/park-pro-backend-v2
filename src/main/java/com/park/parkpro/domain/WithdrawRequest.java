package com.park.parkpro.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "withdraw_request")
public class WithdrawRequest {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_category_id", nullable = false)
    private BudgetCategory budgetCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private WithdrawRequestStatus status = WithdrawRequestStatus.PENDING;

    @Column(name = "audit_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditStatus auditStatus = AuditStatus.UNJUSTIFIED;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

    @Column(name = "currency", nullable = false)
    private String currency = "XAF";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum WithdrawRequestStatus {
        REJECTED, APPROVED, PENDING
    }

    // Constructors
    public WithdrawRequest() {}

    public WithdrawRequest(BigDecimal amount, String reason, String description, User requester,
                           BudgetCategory budgetCategory, Budget budget, String receiptUrl,
                           WithdrawRequestStatus status, Park park) {
        this.amount = amount;
        this.reason = reason;
        this.description = description;
        this.requester = requester;
        this.budgetCategory = budgetCategory;
        this.budget = budget;
        this.receiptUrl = receiptUrl;
        this.status = status;
        this.park = park;
        this.auditStatus = AuditStatus.UNJUSTIFIED;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }
    public BudgetCategory getBudgetCategory() { return budgetCategory; }
    public void setBudgetCategory(BudgetCategory budgetCategory) { this.budgetCategory = budgetCategory; }
    public Budget getBudget() { return budget; }
    public void setBudget(Budget budget) { this.budget = budget; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public WithdrawRequestStatus getStatus() { return status; }
    public void setStatus(WithdrawRequestStatus status) { this.status = status; }
    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Park getPark() { return park; }
    public void setPark(Park park) { this.park = park; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}