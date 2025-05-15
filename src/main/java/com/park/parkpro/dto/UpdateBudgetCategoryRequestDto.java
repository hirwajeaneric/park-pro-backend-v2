package com.park.parkpro.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class UpdateBudgetCategoryRequestDto {
    @NotNull(message = "Allocated amount is required")
    @Min(value = 0, message = "Allocated amount cannot be negative")
    private BigDecimal allocatedAmount;

    // Getters and Setters
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
}