package com.park.parkpro.dto;

import com.park.parkpro.domain.SpendingStrategy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BudgetCategoryResponseDto {
    private UUID id;
    private UUID budgetId;
    private String name;
    private BigDecimal allocatedAmount;
    private BigDecimal usedAmount;
    private BigDecimal balance;
    private SpendingStrategy spendingStrategy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BudgetCategoryResponseDto(UUID id, UUID budgetId, String name, BigDecimal allocatedAmount,
                                     BigDecimal usedAmount, BigDecimal balance, SpendingStrategy spendingStrategy,
                                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.budgetId = budgetId;
        this.name = name;
        this.allocatedAmount = allocatedAmount;
        this.usedAmount = usedAmount;
        this.balance = balance;
        this.spendingStrategy = spendingStrategy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getBudgetId() { return budgetId; }
    public String getName() { return name; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public BigDecimal getUsedAmount() { return usedAmount; }
    public BigDecimal getBalance() { return balance; }
    public SpendingStrategy getSpendingStrategy() { return spendingStrategy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}