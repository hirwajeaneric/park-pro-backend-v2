package com.park.parkpro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateAuditRequestDto {
    @NotNull(message = "Park ID is required")
    private UUID parkId;

    @NotNull(message = "Audit year is required")
    private Integer auditYear;
}

