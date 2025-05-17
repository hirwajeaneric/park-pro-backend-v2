package com.park.parkpro.controller;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.*;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BudgetService;
import com.park.parkpro.service.ExpenseService;
import com.park.parkpro.service.WithdrawRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BudgetController {
    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final WithdrawRequestService withdrawRequestService;

    public BudgetController(BudgetService budgetService, ExpenseService expenseService,
                            WithdrawRequestService withdrawRequestService) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.withdrawRequestService = withdrawRequestService;
    }

    @PostMapping("/parks/{parkId}/budgets")
    public ResponseEntity<BudgetResponseDto> createBudget(
            @PathVariable UUID parkId,
            @Valid @RequestBody CreateBudgetRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Budget budget = budgetService.createBudget(parkId, request.getFiscalYear(), request.getTotalAmount(), "DRAFT", token);
        return ResponseEntity.ok(mapToDto(budget));
    }

    @PatchMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetResponseDto> updateBudget(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateBudgetRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Budget budget = budgetService.updateBudget(budgetId, request.getFiscalYear(), request.getTotalAmount(), request.getStatus(), token);
        return ResponseEntity.ok(mapToDto(budget));
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
        return ResponseEntity.ok(mapToDto(budget));
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
        return ResponseEntity.ok(mapToDto(budget));
    }

    @GetMapping("/budgets/{budgetId}")
    public ResponseEntity<BudgetResponseDto> getBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        Budget budget = budgetService.getBudgetById(budgetId);
        return ResponseEntity.ok(mapToDto(budget));
    }

    @GetMapping("/parks/{parkId}/budgets")
    public ResponseEntity<List<BudgetResponseDto>> getBudgetsByPark(@PathVariable UUID parkId) {
        List<Budget> budgets = budgetService.getBudgetsByPark(parkId);
        return ResponseEntity.ok(budgets.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/budgets/by-fiscal-year/{fiscalYear}")
    public ResponseEntity<List<BudgetByFiscalYearResponseDto>> getBudgetsByFiscalYear(
            @PathVariable Integer fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<BudgetByFiscalYearResponseDto> budgets = budgetService.getBudgetsByFiscalYear(fiscalYear, token);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateExpenseRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.createExpense(request, token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @GetMapping("/parks/{parkId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByPark(
            @PathVariable UUID parkId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Expense> expenses = expenseService.getExpensesByPark(parkId, token);
        return ResponseEntity.ok(expenses.stream().map(this::mapToExpenseDto).collect(Collectors.toList()));
    }

    @GetMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Expense> expenses = expenseService.getExpensesByBudget(budgetId, token);
        return ResponseEntity.ok(expenses.stream().map(this::mapToExpenseDto).collect(Collectors.toList()));
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> getExpenseById(
            @PathVariable UUID expenseId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.getExpenseById(expenseId, token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @GetMapping("/budgets/{budgetId}/expenses/my-submissions")
    public ResponseEntity<List<ExpenseResponseDto>> getMySubmittedExpenses(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Expense> expenses = expenseService.getExpensesByCreatedBy(budgetId, token);
        return ResponseEntity.ok(expenses.stream().map(this::mapToExpenseDto).collect(Collectors.toList()));
    }

    @PatchMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdateExpenseRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.updateExpense(expenseId, request, token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @PatchMapping("/expenses/{expenseId}/audit-status")
    public ResponseEntity<ExpenseResponseDto> updateExpenseAuditStatus(
            @PathVariable UUID expenseId,
            @Valid @RequestBody UpdateAuditStatusDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.updateAuditStatus(expenseId, request, token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID expenseId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        expenseService.deleteExpense(expenseId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/budgets/categories/{categoryId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByBudgetCategory(
            @PathVariable UUID categoryId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Expense> expenses = expenseService.getExpensesByBudgetCategory(categoryId, token);
        return ResponseEntity.ok(expenses.stream().map(this::mapToExpenseDto).collect(Collectors.toList()));
    }

    @PostMapping("/budgets/{budgetId}/withdraw-requests")
    public ResponseEntity<WithdrawRequestResponseDto> createWithdrawRequest(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateWithdrawRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.createWithdrawRequest(budgetId, request, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @GetMapping("/budgets/withdraw-requests/{withdrawRequestId}/approve")
    public ResponseEntity<WithdrawRequestResponseDto> approveWithdrawRequest(
            @PathVariable UUID withdrawRequestId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.approveWithdrawRequest(withdrawRequestId, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @PostMapping("/budgets/withdraw-requests/{withdrawRequestId}/reject")
    public ResponseEntity<WithdrawRequestResponseDto> rejectWithdrawRequest(
            @PathVariable UUID withdrawRequestId,
            @RequestParam(required = false) String rejectionReason,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.rejectWithdrawRequest(withdrawRequestId, rejectionReason, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @GetMapping("/budgets/{budgetId}/withdraw-requests")
    public ResponseEntity<List<WithdrawRequestResponseDto>> getWithdrawRequestsByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<WithdrawRequest> requests = withdrawRequestService.getWithdrawRequestsByBudget(budgetId, token);
        return ResponseEntity.ok(requests.stream().map(this::mapToWithdrawRequestDto).collect(Collectors.toList()));
    }

    @GetMapping("/budgets/{budgetId}/withdraw-requests/my-submissions")
    public ResponseEntity<List<WithdrawRequestResponseDto>> getMySubmittedWithdrawRequests(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<WithdrawRequest> requests = withdrawRequestService.getWithdrawRequestsByRequester(budgetId, token);
        return ResponseEntity.ok(requests.stream().map(this::mapToWithdrawRequestDto).collect(Collectors.toList()));
    }

    @GetMapping("/withdraw-requests/{withdrawRequestId}")
    public ResponseEntity<WithdrawRequestResponseDto> getWithdrawRequestById(
            @PathVariable UUID withdrawRequestId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest request = withdrawRequestService.getWithdrawRequestById(withdrawRequestId, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(request));
    }

    @PatchMapping("/withdraw-requests/{withdrawRequestId}")
    public ResponseEntity<WithdrawRequestResponseDto> updateWithdrawRequest(
            @PathVariable UUID withdrawRequestId,
            @Valid @RequestBody UpdateWithdrawRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.updateWithdrawRequest(withdrawRequestId, request, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @PatchMapping("/withdraw-requests/{withdrawRequestId}/audit-status")
    public ResponseEntity<WithdrawRequestResponseDto> updateWithdrawRequestAuditStatus(
            @PathVariable UUID withdrawRequestId,
            @Valid @RequestBody UpdateAuditStatusDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.updateAuditStatus(withdrawRequestId, request, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @DeleteMapping("/withdraw-requests/{withdrawRequestId}")
    public ResponseEntity<Void> deleteWithdrawRequest(
            @PathVariable UUID withdrawRequestId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        withdrawRequestService.deleteWithdrawRequest(withdrawRequestId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/withdraw-requests")
    public ResponseEntity<List<WithdrawRequestResponseDto>> getWithdrawRequestsByBudgetCategory(
            @PathVariable UUID budgetId,
            @PathVariable UUID categoryId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<WithdrawRequest> requests = withdrawRequestService.getWithdrawRequestsByBudgetCategory(categoryId);
        return ResponseEntity.ok(requests.stream().map(this::mapToWithdrawRequestDto).collect(Collectors.toList()));
    }

    private BudgetResponseDto mapToDto(Budget budget) {
        return new BudgetResponseDto(
                budget.getId(),
                budget.getPark().getId(),
                budget.getPark().getName(),
                budget.getFiscalYear(),
                budget.getTotalAmount(),
                budget.getBalance(),
                budget.getUnallocated(),
                budget.getStatus(),
                budget.getCreatedBy().getId(),
                budget.getApprovedBy() != null ? budget.getApprovedBy().getId() : null,
                budget.getApprovedAt(),
                budget.getCreatedAt(),
                budget.getUpdatedAt());
    }

    private BudgetByFiscalYearResponseDto mapToFiscalYearDto(Budget budget) {
        return new BudgetByFiscalYearResponseDto(
                budget.getId(),
                budget.getPark().getId(),
                budget.getPark().getName(),
                budget.getFiscalYear(),
                budget.getTotalAmount(),
                budget.getBalance(),
                budget.getUnallocated(),
                budget.getStatus(),
                budget.getCreatedBy().getId(),
                budget.getApprovedBy() != null ? budget.getApprovedBy().getId() : null,
                budget.getApprovedAt(),
                budget.getCreatedAt(),
                budget.getUpdatedAt());
    }

    private ExpenseResponseDto mapToExpenseDto(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(), expense.getBudget().getId(), expense.getAmount(), expense.getDescription(),
                expense.getBudgetCategory().getId(), expense.getBudgetCategory().getName(),
                expense.getPark().getId(), expense.getPark().getName(), expense.getCreatedBy() != null ? expense.getCreatedBy().getId() : null,
                expense.getAuditStatus(), expense.getJustification(), expense.getReceiptUrl(), expense.getCurrency(),
                expense.getCreatedAt(), expense.getUpdatedAt());
    }

    private WithdrawRequestResponseDto mapToWithdrawRequestDto(WithdrawRequest request) {
        return new WithdrawRequestResponseDto(
                request.getId(), request.getAmount(), request.getReason(), request.getDescription(),
                request.getRequester().getId(), request.getApprover() != null ? request.getApprover().getId() : null,
                request.getBudgetCategory().getId(), request.getBudgetCategory().getName(),
                request.getBudget().getId(), request.getReceiptUrl(), request.getStatus(), request.getAuditStatus(),
                request.getJustification(), request.getApprovedAt(), request.getRejectionReason(), request.getPark().getId(), request.getPark().getName(), request.getCurrency(),
                request.getCreatedAt(), request.getUpdatedAt());
    }
}