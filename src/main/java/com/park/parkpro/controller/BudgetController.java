package com.park.parkpro.controller;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.dto.BudgetResponseDto;
import com.park.parkpro.dto.CreateBudgetRequestDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/parks/{parkId}/budgets")
    public ResponseEntity<BudgetResponseDto> createBudget(
            @PathVariable UUID parkId,
            @RequestBody CreateBudgetRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Budget budget = budgetService.createBudget(parkId, request.getFiscalYear(), request.getTotalAmount(), "DRAFT", token);
        BudgetResponseDto response = mapToDto(budget);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetResponseDto> updateBudget(
            @PathVariable UUID budgetId,
            @RequestBody CreateBudgetRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Budget budget = budgetService.updateBudget(budgetId, request.getTotalAmount(), request.getStatus(), token);
        BudgetResponseDto response = mapToDto(budget);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/budgets/{budgetId}/approve")
    public ResponseEntity<BudgetResponseDto> approveBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Budget budget = budgetService.approveBudget(budgetId, token);
        BudgetResponseDto response = mapToDto(budget);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/budgets/{budgetId}/reject")
    public ResponseEntity<BudgetResponseDto> rejectBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Budget budget = budgetService.rejectBudget(budgetId, token);
        BudgetResponseDto response = mapToDto(budget);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/parks/{parkId}/budgets")
    public ResponseEntity<List<BudgetResponseDto>> getBudgetsByPark(@PathVariable UUID parkId) {
        List<Budget> budgets = budgetService.getBudgetsByPark(parkId);
        List<BudgetResponseDto> response = budgets.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private BudgetResponseDto mapToDto(Budget budget) {
        return new BudgetResponseDto(
                budget.getId(),
                budget.getPark().getId(),
                budget.getFiscalYear(),
                budget.getTotalAmount(),
                budget.getBalance(),
                budget.getStatus(),
                budget.getCreatedBy().getId(),
                budget.getApprovedBy() != null ? budget.getApprovedBy().getId() : null,
                budget.getApprovedAt(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}