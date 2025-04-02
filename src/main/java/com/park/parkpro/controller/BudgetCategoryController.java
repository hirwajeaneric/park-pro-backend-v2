package com.park.parkpro.controller;

import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.dto.BudgetCategoryRequestDto;
import com.park.parkpro.dto.BudgetCategoryResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BudgetCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets/{budgetId}/categories")
public class BudgetCategoryController {
    private final BudgetCategoryService budgetCategoryService;

    public BudgetCategoryController(BudgetCategoryService budgetCategoryService) {
        this.budgetCategoryService = budgetCategoryService;
    }

    @PostMapping
    public ResponseEntity<BudgetCategoryResponseDto> createCategory(
            @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetCategoryRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        BudgetCategory category = budgetCategoryService.createBudgetCategory(budgetId, request.getName(), request.getAllocatedAmount(), token);
        BudgetCategoryResponseDto response = mapToDto(category);
        return ResponseEntity.created(URI.create("/api/budgets/" + budgetId + "/categories/" + category.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetCategoryResponseDto>> getCategoriesByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        List<BudgetCategory> categories = budgetCategoryService.getCategoriesByBudget(budgetId);
        List<BudgetCategoryResponseDto> response = categories.stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<BudgetCategoryResponseDto> getCategory(
            @PathVariable UUID categoryId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        BudgetCategory category = budgetCategoryService.getCategoryById(categoryId);
        BudgetCategoryResponseDto response = mapToDto(category);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<BudgetCategoryResponseDto> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody BudgetCategoryRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        BudgetCategory category = budgetCategoryService.updateBudgetCategory(categoryId, request.getName(), request.getAllocatedAmount(), token);
        BudgetCategoryResponseDto response = mapToDto(category);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
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
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}