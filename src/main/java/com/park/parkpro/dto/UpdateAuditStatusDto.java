package com.park.parkpro.dto;

import com.park.parkpro.domain.AuditStatus;
import jakarta.validation.constraints.Size;

public class UpdateAuditStatusDto {
    private AuditStatus auditStatus;

    @Size(max = 500, message = "Justification must not exceed 500 characters")
    private String justification;

    public UpdateAuditStatusDto() {}

    public UpdateAuditStatusDto(AuditStatus auditStatus, String justification) {
        this.auditStatus = auditStatus;
        this.justification = justification;
    }

    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    // Validate that justification is provided for UNJUSTIFIED or FAILED
    public void validate() {
        if (auditStatus == null) {
            throw new IllegalArgumentException("Audit status is required");
        }
        if ((auditStatus == AuditStatus.UNJUSTIFIED || auditStatus == AuditStatus.FAILED) &&
                (justification == null || justification.trim().isEmpty())) {
            throw new IllegalArgumentException("Justification is required for UNJUSTIFIED or FAILED audit status");
        }
        if (auditStatus == AuditStatus.PASSED && justification != null && !justification.trim().isEmpty()) {
            throw new IllegalArgumentException("Justification should not be provided for PASSED audit status");
        }
    }
}