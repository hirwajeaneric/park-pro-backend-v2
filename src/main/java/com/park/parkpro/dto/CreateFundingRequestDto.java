package com.park.parkpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateFundingRequestDto {
    @NotNull
    private UUID parkId;

    @NotNull
    @Positive
    private BigDecimal requestedAmount;

    @NotBlank
    private String requestType;

    @NotBlank
    private String reason;

    @NotNull
    private UUID budgetId;

    @NotNull
    private UUID budgetCategoryId;

    // Getters and Setters
    public UUID getParkId() { return parkId; }
    public void setParkId(UUID parkId) { this.parkId = parkId; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getBudgetId() { return budgetId; }
    public void setBudgetId(UUID budgetId) { this.budgetId = budgetId; }
    public UUID getBudgetCategoryId() { return budgetCategoryId; }
    public void setBudgetCategoryId(UUID budgetCategoryId) { this.budgetCategoryId = budgetCategoryId; }
}