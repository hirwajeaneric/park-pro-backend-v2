// src/main/java/com/park/parkpro/controller/OpportunityApplicationController.java
package com.park.parkpro.controller;

import com.park.parkpro.domain.OpportunityApplication;
import com.park.parkpro.dto.CreateOpportunityApplicationRequestDto;
import com.park.parkpro.dto.OpportunityApplicationResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.OpportunityApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/opportunity-applications")
public class OpportunityApplicationController {
    private final OpportunityApplicationService applicationService;

    public OpportunityApplicationController(OpportunityApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<OpportunityApplicationResponseDto> createApplication(
            @Valid @RequestBody CreateOpportunityApplicationRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        OpportunityApplication application = applicationService.createApplication(
                request.getOpportunityId(), request.getFirstName(), request.getLastName(),
                request.getEmail(), request.getApplicationLetterUrl(), token);
        return ResponseEntity.created(URI.create("/api/opportunity-applications/" + application.getId()))
                .body(mapToApplicationDto(application));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<OpportunityApplicationResponseDto> updateApplicationStatus(
            @PathVariable UUID applicationId,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        OpportunityApplication application = applicationService.updateApplicationStatus(applicationId, status, token);
        return ResponseEntity.ok(mapToApplicationDto(application));
    }

    @GetMapping("/opportunity/{opportunityId}")
    public ResponseEntity<List<OpportunityApplicationResponseDto>> getApplicationsByOpportunity(
            @PathVariable UUID opportunityId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<OpportunityApplication> applications = applicationService.getApplicationsByOpportunity(opportunityId, token);
        return ResponseEntity.ok(applications.stream().map(this::mapToApplicationDto).collect(Collectors.toList()));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<OpportunityApplicationResponseDto> getApplicationById(@PathVariable UUID applicationId) {
        OpportunityApplication application = applicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(mapToApplicationDto(application));
    }

    private OpportunityApplicationResponseDto mapToApplicationDto(OpportunityApplication application) {
        return new OpportunityApplicationResponseDto(
                application.getId(), application.getOpportunity().getId(), application.getFirstName(),
                application.getLastName(), application.getEmail(), application.getApplicationLetterUrl(),
                application.getStatus(), application.getCreatedAt(), application.getUpdatedAt()
        );
    }
}