package com.park.parkpro.dto;

import com.park.parkpro.domain.SpendingStrategy;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateBudgetCategoryRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Percentage is required")
    @Min(value = 0, message = "Percentage cannot be negative")
    @Max(value = 100, message = "Percentage cannot exceed 100")
    private BigDecimal percentage;

    @NotNull(message = "Spending strategy is required")
    private SpendingStrategy spendingStrategy;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public SpendingStrategy getSpendingStrategy() { return spendingStrategy; }
    public void setSpendingStrategy(SpendingStrategy spendingStrategy) { this.spendingStrategy = spendingStrategy; }
}