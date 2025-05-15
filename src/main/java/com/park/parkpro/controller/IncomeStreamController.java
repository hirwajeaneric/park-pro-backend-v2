package com.park.parkpro.controller;

import com.park.parkpro.domain.IncomeStream;
import com.park.parkpro.dto.IncomeStreamResponseDto;
import com.park.parkpro.dto.CreateIncomeStreamRequestDto;
import com.park.parkpro.dto.UpdateIncomeStreamRequestDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.IncomeStreamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class IncomeStreamController {
    private final IncomeStreamService incomeStreamService;

    public IncomeStreamController(IncomeStreamService incomeStreamService) {
        this.incomeStreamService = incomeStreamService;
    }

    @PostMapping("/budgets/{budgetId}/income-streams")
    public ResponseEntity<IncomeStreamResponseDto> createIncomeStream(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateIncomeStreamRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        IncomeStream incomeStream = incomeStreamService.createIncomeStream(
                budgetId, request.getName(), request.getPercentage(), request.getTotalContribution(), token);
        return ResponseEntity.ok(mapToDto(incomeStream));
    }

    @PatchMapping("/income-streams/{incomeStreamId}")
    public ResponseEntity<IncomeStreamResponseDto> updateIncomeStream(
            @PathVariable UUID incomeStreamId,
            @Valid @RequestBody UpdateIncomeStreamRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        IncomeStream incomeStream = incomeStreamService.updateIncomeStream(
                incomeStreamId, request.getName(), request.getPercentage(), request.getTotalContribution(), token);
        return ResponseEntity.ok(mapToDto(incomeStream));
    }

    @GetMapping("/budgets/{budgetId}/income-streams")
    public ResponseEntity<List<IncomeStreamResponseDto>> getIncomeStreamsByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        List<IncomeStream> incomeStreams = incomeStreamService.getIncomeStreamsByBudget(budgetId);
        return ResponseEntity.ok(incomeStreams.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/parks/{parkId}/income-streams/fiscal-year/{fiscalYear}")
    public ResponseEntity<List<IncomeStreamResponseDto>> getIncomeStreamsByParkAndFiscalYear(
            @PathVariable UUID parkId,
            @PathVariable Integer fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        List<IncomeStream> incomeStreams = incomeStreamService.getIncomeStreamsByParkAndFiscalYear(parkId, fiscalYear);
        return ResponseEntity.ok(incomeStreams.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/income-streams/{incomeStreamId}")
    public ResponseEntity<Void> deleteIncomeStream(
            @PathVariable UUID incomeStreamId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        incomeStreamService.deleteIncomeStream(incomeStreamId, token);
        return ResponseEntity.noContent().build();
    }

    private IncomeStreamResponseDto mapToDto(IncomeStream incomeStream) {
        return new IncomeStreamResponseDto(
                incomeStream.getId(),
                incomeStream.getBudget().getId(),
                incomeStream.getPark().getId(),
                incomeStream.getFiscalYear(),
                incomeStream.getName(),
                incomeStream.getPercentage(),
                incomeStream.getTotalContribution(),
                incomeStream.getActualBalance(),
                incomeStream.getCreatedBy().getId(),
                incomeStream.getCreatedAt(),
                incomeStream.getUpdatedAt()
        );
    }
}