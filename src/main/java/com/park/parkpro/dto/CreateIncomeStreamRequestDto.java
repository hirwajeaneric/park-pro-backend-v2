package com.park.parkpro.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateIncomeStreamRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Percentage is required")
    @Min(value = 0, message = "Percentage cannot be negative")
    @Max(value = 100, message = "Percentage cannot exceed 100")
    private BigDecimal percentage;

    @NotNull(message = "Total contribution is required")
    @Min(value = 0, message = "Total contribution cannot be negative")
    private BigDecimal totalContribution;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public BigDecimal getTotalContribution() { return totalContribution; }
    public void setTotalContribution(BigDecimal totalContribution) { this.totalContribution = totalContribution; }
}