package com.park.parkpro.dto;

import com.park.parkpro.domain.SpendingStrategy;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class UpdateBudgetCategoryRequestDto {
    @NotNull(message = "Allocated amount is required")
    @Min(value = 0, message = "Allocated amount cannot be negative")
    private BigDecimal allocatedAmount;
    private SpendingStrategy spendingStrategy;

    // Getters and Setters
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public SpendingStrategy getSpendingStrategy() { return spendingStrategy; }
}