package com.park.parkpro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateOpportunityApplicationStatusRequestDto {
    @NotNull
    private String status;

    @Size(max = 1000)
    private String approvalMessage;

    @Size(max = 1000)
    private String rejectionReason;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApprovalMessage() { return approvalMessage; }
    public void setApprovalMessage(String approvalMessage) { this.approvalMessage = approvalMessage; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}