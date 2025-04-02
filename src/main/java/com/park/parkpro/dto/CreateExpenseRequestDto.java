package com.park.parkpro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateExpenseRequestDto {
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Budget category ID is required")
    private UUID budgetCategoryId;

    @NotNull(message = "Park ID is required")
    private UUID parkId;

    // Optional field
    private String receiptUrl;

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public UUID getBudgetCategoryId() { return budgetCategoryId; }
    public void setBudgetCategoryId(UUID budgetCategoryId) { this.budgetCategoryId = budgetCategoryId; }
    public UUID getParkId() { return parkId; }
    public void setParkId(UUID parkId) { this.parkId = parkId; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
}