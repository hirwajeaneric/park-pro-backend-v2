package com.park.parkpro.dto;

import java.math.BigDecimal;

public class CreateBudgetRequestDto {
    private Integer fiscalYear;
    private BigDecimal totalAmount;
    private String status;

    // Getters and Setters
    public Integer getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(Integer fiscalYear) { this.fiscalYear = fiscalYear; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}