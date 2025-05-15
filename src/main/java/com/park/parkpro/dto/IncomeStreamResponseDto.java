package com.park.parkpro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class IncomeStreamResponseDto {
    private UUID id;
    private UUID budgetId;
    private UUID parkId;
    private Integer fiscalYear;
    private String name;
    private BigDecimal percentage;
    private BigDecimal totalContribution;
    private BigDecimal actualBalance;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IncomeStreamResponseDto(UUID id, UUID budgetId, UUID parkId, Integer fiscalYear, String name,
                                   BigDecimal percentage, BigDecimal totalContribution, BigDecimal actualBalance,
                                   UUID createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.budgetId = budgetId;
        this.parkId = parkId;
        this.fiscalYear = fiscalYear;
        this.name = name;
        this.percentage = percentage;
        this.totalContribution = totalContribution;
        this.actualBalance = actualBalance;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getBudgetId() { return budgetId; }
    public UUID getParkId() { return parkId; }
    public Integer getFiscalYear() { return fiscalYear; }
    public String getName() { return name; }
    public BigDecimal getPercentage() { return percentage; }
    public BigDecimal getTotalContribution() { return totalContribution; }
    public BigDecimal getActualBalance() { return actualBalance; }
    public UUID getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}