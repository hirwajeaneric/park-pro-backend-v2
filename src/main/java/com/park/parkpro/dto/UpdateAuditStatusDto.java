package com.park.parkpro.dto;

import com.park.parkpro.domain.AuditStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateAuditStatusDto {
    @NotNull(message = "Audit status is required")
    private AuditStatus auditStatus;

    // Getters and Setters
    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
}