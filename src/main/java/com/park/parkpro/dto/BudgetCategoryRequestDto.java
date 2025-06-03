package com.park.parkpro.dto;

import com.park.parkpro.domain.SpendingStrategy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BudgetCategoryRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Allocated amount is required")
    @Min(value = 0, message = "Allocated amount cannot be negative")
    private BigDecimal allocatedAmount;

    @NotNull(message = "Spending strategy is required")
    private SpendingStrategy spendingStrategy;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public SpendingStrategy getSpendingStrategy() { return spendingStrategy; }
    public void setSpendingStrategy(SpendingStrategy spendingStrategy) { this.spendingStrategy = spendingStrategy; }
}