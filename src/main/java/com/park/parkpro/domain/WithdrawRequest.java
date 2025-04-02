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

    @Column(name = "status", nullable = false)
    private String status;

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

    // Constructors
    public WithdrawRequest() {}

    public WithdrawRequest(BigDecimal amount, String reason, String description, User requester,
                           BudgetCategory budgetCategory, String status, Park park) {
        this.amount = amount;
        this.reason = reason;
        this.description = description;
        this.requester = requester;
        this.budgetCategory = budgetCategory;
        this.status = status;
        this.park = park;
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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