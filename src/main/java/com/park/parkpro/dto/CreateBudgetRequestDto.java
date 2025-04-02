package com.park.parkpro.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateBudgetRequestDto {
    @NotNull(message = "Fiscal year is required")
    @Min(value = 2000, message = "Fiscal year must be 2000 or later")
    private Integer fiscalYear;

    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    @NotBlank(message = "Status is required")
    private String status;

    // Getters and Setters
    public Integer getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(Integer fiscalYear) { this.fiscalYear = fiscalYear; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}