package com.park.parkpro.dto;

import com.park.parkpro.domain.AuditProgress;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAuditRequestDto {
    @NotNull(message = "Audit progress is required")
    private AuditProgress auditProgress;
}

