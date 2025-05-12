package com.park.parkpro.controller;

import com.park.parkpro.dto.IncomeStreamRequestDto;
import com.park.parkpro.dto.IncomeStreamResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.IncomeStreamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
            @Valid @RequestBody IncomeStreamRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        IncomeStreamResponseDto response = incomeStreamService.createIncomeStream(budgetId, request, token);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/income-streams/{incomeStreamId}")
    public ResponseEntity<IncomeStreamResponseDto> updateIncomeStream(
            @PathVariable UUID incomeStreamId,
            @Valid @RequestBody IncomeStreamRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        IncomeStreamResponseDto response = incomeStreamService.updateIncomeStream(incomeStreamId, request, token);
        return ResponseEntity.ok(response);
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

    @GetMapping("/income-streams/{incomeStreamId}")
    public ResponseEntity<IncomeStreamResponseDto> getIncomeStream(
            @PathVariable UUID incomeStreamId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        IncomeStreamResponseDto response = incomeStreamService.getIncomeStreamById(incomeStreamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budgets/{budgetId}/income-streams/fiscal-year/{fiscalYear}")
    public ResponseEntity<List<IncomeStreamResponseDto>> getIncomeStreamsByBudgetAndFiscalYear(
            @PathVariable UUID budgetId,
            @PathVariable int fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<IncomeStreamResponseDto> response = incomeStreamService.getIncomeStreamsByBudgetAndFiscalYear(budgetId, fiscalYear, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/budgets/{budgetId}/income-streams")
    public ResponseEntity<List<IncomeStreamResponseDto>> getIncomeStreamsByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        List<IncomeStreamResponseDto> response = incomeStreamService.getIncomeStreamsByBudget(budgetId);
        return ResponseEntity.ok(response);
    }
}