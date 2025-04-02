package com.park.parkpro.dto;

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

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
}