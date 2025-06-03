package com.park.parkpro.dto;

import com.park.parkpro.domain.AuditProgress;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuditResponseDto {
    private UUID id;
    private UUID parkId;
    private String parkName;
    private Integer auditYear;
    private Double percentagePassed;
    private Double percentageFailed;
    private Double percentageUnjustified;
    private Double totalPercentage;
    private AuditProgress auditProgress;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

