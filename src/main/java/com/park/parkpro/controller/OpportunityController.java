// src/main/java/com/park/parkpro/controller/OpportunityController.java
package com.park.parkpro.controller;

import com.park.parkpro.domain.Opportunity;
import com.park.parkpro.dto.CreateOpportunityRequestDto;
import com.park.parkpro.dto.OpportunityResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OpportunityController {
    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @PostMapping("/opportunities")
    public ResponseEntity<OpportunityResponseDto> createOpportunity(
            @Valid @RequestBody CreateOpportunityRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        System.out.println("Request: " + request);
        Opportunity opportunity = opportunityService.createOpportunity(
                request.getTitle(), request.getDescription(), request.getDetails(),
                request.getType(), request.getStatus(), request.getVisibility(),
                request.getParkId(), token);
        return ResponseEntity.created(URI.create("/api/opportunities/" + opportunity.getId()))
                .body(mapToOpportunityDto(opportunity));
    }

    @PatchMapping("/opportunities/{opportunityId}")
    public ResponseEntity<OpportunityResponseDto> updateOpportunity(
            @PathVariable UUID opportunityId,
            @Valid @RequestBody CreateOpportunityRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Opportunity opportunity = opportunityService.updateOpportunity(
                opportunityId, request.getTitle(), request.getDescription(), request.getDetails(),
                request.getStatus(), request.getVisibility(), request.getParkId(), token);
        return ResponseEntity.ok(mapToOpportunityDto(opportunity));
    }

    @GetMapping("/opportunities")
    public ResponseEntity<List<OpportunityResponseDto>> getAllOpportunities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        List<Opportunity> opportunities = opportunityService.getAllOpportunities(token);
        return ResponseEntity.ok(opportunities.stream().map(this::mapToOpportunityDto).collect(Collectors.toList()));
    }

    @GetMapping("/opportunities/{opportunityId}")
    public ResponseEntity<OpportunityResponseDto> getOpportunityById(
            @PathVariable UUID opportunityId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        Opportunity opportunity = opportunityService.getOpportunityById(opportunityId, token);
        return ResponseEntity.ok(mapToOpportunityDto(opportunity));
    }

    @GetMapping("/opportunities/my")
    public ResponseEntity<List<OpportunityResponseDto>> getMyOpportunities(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Opportunity> opportunities = opportunityService.getOpportunitiesByCreator(token);
        return ResponseEntity.ok(opportunities.stream().map(this::mapToOpportunityDto).collect(Collectors.toList()));
    }

    @GetMapping("/park/{parkId}/opportunities")
    public ResponseEntity<List<OpportunityResponseDto>> getOpportunitiesByParkId(
            @PathVariable UUID parkId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        List<Opportunity> opportunities = opportunityService.getOpportunitiesByParkId(parkId, token);
        return ResponseEntity.ok(opportunities.stream().map(this::mapToOpportunityDto).collect(Collectors.toList()));
    }

    private OpportunityResponseDto mapToOpportunityDto(Opportunity opportunity) {
        return new OpportunityResponseDto(
                opportunity.getId(), opportunity.getTitle(), opportunity.getDescription(), opportunity.getDetails(),
                opportunity.getType(), opportunity.getStatus(), opportunity.getVisibility(),
                opportunity.getCreatedBy().getId(), opportunity.getPark().getId(), opportunity.getPark().getName(),
                opportunity.getCreatedAt(), opportunity.getUpdatedAt()
        );
    }
}