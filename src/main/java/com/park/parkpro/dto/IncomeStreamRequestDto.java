package com.park.parkpro.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class IncomeStreamRequestDto {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Percentage must be at least 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Percentage must be at most 100")
    private BigDecimal percentage;

    @NotNull(message = "Total contribution is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total contribution must be non-negative")
    private BigDecimal totalContribution;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    public BigDecimal getTotalContribution() { return totalContribution; }
    public void setTotalContribution(BigDecimal totalContribution) { this.totalContribution = totalContribution; }
}