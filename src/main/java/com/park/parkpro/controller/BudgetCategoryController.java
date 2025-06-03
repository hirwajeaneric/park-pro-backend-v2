package com.park.parkpro.controller;

import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.dto.BudgetCategoryResponseDto;
import com.park.parkpro.dto.CreateBudgetCategoryRequestDto;
import com.park.parkpro.dto.UpdateBudgetCategoryRequestDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BudgetCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BudgetCategoryController {
    private final BudgetCategoryService budgetCategoryService;

    public BudgetCategoryController(BudgetCategoryService budgetCategoryService) {
        this.budgetCategoryService = budgetCategoryService;
    }

    @PostMapping("/budgets/{budgetId}/categories")
    public ResponseEntity<BudgetCategoryResponseDto> createBudgetCategory(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateBudgetCategoryRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        BudgetCategory category = budgetCategoryService.createBudgetCategory(budgetId, request.getName(), 
            request.getPercentage(), request.getSpendingStrategy(), token);
        return ResponseEntity.ok(mapToDto(category));
    }

    @PatchMapping("/budgets/{budgetId}/categories/{categoryId}")
    public ResponseEntity<BudgetCategoryResponseDto> updateBudgetCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateBudgetCategoryRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        BudgetCategory category = budgetCategoryService.updateBudgetCategory(categoryId, request.getAllocatedAmount(), 
            request.getSpendingStrategy(), token);
        return ResponseEntity.ok(mapToDto(category));
    }

    @GetMapping("/budgets/{budgetId}/categories")
    public ResponseEntity<List<BudgetCategoryResponseDto>> getBudgetCategoriesByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        List<BudgetCategory> categories = budgetCategoryService.getBudgetCategoriesByBudget(budgetId);
        return ResponseEntity.ok(categories.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/budgets/{budgetId}/categories/{categoryId}")
    public ResponseEntity<Void> deleteBudgetCategory(
            @PathVariable UUID categoryId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        budgetCategoryService.deleteBudgetCategory(categoryId, token);
        return ResponseEntity.noContent().build();
    }

    private BudgetCategoryResponseDto mapToDto(BudgetCategory category) {
        return new BudgetCategoryResponseDto(
                category.getId(),
                category.getBudget().getId(),
                category.getName(),
                category.getAllocatedAmount(),
                category.getUsedAmount(),
                category.getBalance(),
                category.getSpendingStrategy(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}