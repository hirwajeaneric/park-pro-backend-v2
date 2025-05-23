package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BudgetResponseDto {
    private UUID id;
    private UUID parkId;
    private String parkName;
    private Integer fiscalYear;
    private BigDecimal totalAmount;
    private BigDecimal balance;
    private BigDecimal unallocated; // Added
    private String status;
    private UUID createdBy;
    private UUID approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BudgetResponseDto(UUID id, UUID parkId, String parkName, Integer fiscalYear, BigDecimal totalAmount,
                             BigDecimal balance, BigDecimal unallocated, String status, UUID createdBy, UUID approvedBy,
                             LocalDateTime approvedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.parkId = parkId;
        this.parkName = parkName;
        this.fiscalYear = fiscalYear;
        this.totalAmount = totalAmount;
        this.balance = balance;
        this.unallocated = unallocated;
        this.status = status;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getParkId() { return parkId; }
    public String getParkName() { return parkName; }
    public Integer getFiscalYear() { return fiscalYear; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getUnallocated() { return unallocated; }
    public String getStatus() { return status; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}