package com.park.parkpro.controller;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.*;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BudgetService;
import com.park.parkpro.service.ExpenseService;
import com.park.parkpro.service.FundingRequestService;
import com.park.parkpro.service.WithdrawRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BudgetController {
    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final WithdrawRequestService withdrawRequestService;
    private final FundingRequestService fundingRequestService; // New dependency

    public BudgetController(BudgetService budgetService, ExpenseService expenseService, WithdrawRequestService withdrawRequestService, FundingRequestService fundingRequestService) {
        this.budgetService = budgetService;
        this.expenseService = expenseService;
        this.withdrawRequestService = withdrawRequestService;
        this.fundingRequestService = fundingRequestService;
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
        BudgetResponseDto response = mapToDto(budget);
        return ResponseEntity.ok(response);
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

    // Expense Endpoints
    @PostMapping("/budgets/{budgetId}/expenses")
    public ResponseEntity<ExpenseResponseDto> createExpense(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateExpenseRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.createExpense(budgetId, request.getAmount(), request.getDescription(),
                request.getCategory(), request.getBudgetCategoryId(), request.getParkId(), request.getReceiptUrl(), token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @PostMapping("/budgets/{budgetId}/expenses/{expenseId}/approve")
    public ResponseEntity<ExpenseResponseDto> approveExpense(
            @PathVariable UUID budgetId,
            @PathVariable UUID expenseId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.approveExpense(expenseId, token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @PostMapping("/budgets/{budgetId}/expenses/{expenseId}/reject")
    public ResponseEntity<ExpenseResponseDto> rejectExpense(
            @PathVariable UUID budgetId,
            @PathVariable UUID expenseId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Expense expense = expenseService.rejectExpense(expenseId, token);
        return ResponseEntity.ok(mapToExpenseDto(expense));
    }

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByBudgetCategory(
            @PathVariable UUID budgetId,
            @PathVariable UUID categoryId) {
        List<Expense> expenses = expenseService.getExpensesByBudgetCategory(categoryId);
        return ResponseEntity.ok(expenses.stream().map(this::mapToExpenseDto).collect(Collectors.toList()));
    }

    // Withdraw Request Endpoints
    @PostMapping("/budgets/{budgetId}/withdraw-requests")
    public ResponseEntity<WithdrawRequestResponseDto> createWithdrawRequest(
            @PathVariable UUID budgetId,
            @Valid @RequestBody CreateWithdrawRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.createWithdrawRequest(budgetId, request.getAmount(),
                request.getReason(), request.getDescription(), request.getBudgetCategoryId(), request.getParkId(), token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @PostMapping("/budgets/{budgetId}/withdraw-requests/{withdrawRequestId}/approve")
    public ResponseEntity<WithdrawRequestResponseDto> approveWithdrawRequest(
            @PathVariable UUID budgetId,
            @PathVariable UUID withdrawRequestId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        WithdrawRequest withdrawRequest = withdrawRequestService.approveWithdrawRequest(withdrawRequestId, token);
        return ResponseEntity.ok(mapToWithdrawRequestDto(withdrawRequest));
    }

    @PostMapping("/budgets/{budgetId}/withdraw-requests/{withdrawRequestId}/reject")
    public ResponseEntity<WithdrawRequestResponseDto> rejectWithdrawRequest(
            @PathVariable UUID budgetId,
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

    @GetMapping("/budgets/{budgetId}/categories/{categoryId}/withdraw-requests")
    public ResponseEntity<List<WithdrawRequestResponseDto>> getWithdrawRequestsByBudgetCategory(
            @PathVariable UUID budgetId,
            @PathVariable UUID categoryId) {
        List<WithdrawRequest> requests = withdrawRequestService.getWithdrawRequestsByBudgetCategory(categoryId);
        return ResponseEntity.ok(requests.stream().map(this::mapToWithdrawRequestDto).collect(Collectors.toList()));
    }

    @PostMapping("/parks/{parkId}/funding-requests")
    public ResponseEntity<FundingRequestResponseDto> createFundingRequest(
            @PathVariable UUID parkId,
            @Valid @RequestBody CreateFundingRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        FundingRequest fundingRequest = fundingRequestService.createFundingRequest(
                parkId, request.getRequestedAmount(), request.getRequestType(), request.getReason(),
                request.getBudgetId(), token);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @PostMapping("/funding-requests/{fundingRequestId}/approve")
    public ResponseEntity<FundingRequestResponseDto> approveFundingRequest(
            @PathVariable UUID fundingRequestId,
            @RequestParam BigDecimal approvedAmount,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        FundingRequest fundingRequest = fundingRequestService.approveFundingRequest(fundingRequestId, approvedAmount, token);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @PostMapping("/funding-requests/{fundingRequestId}/reject")
    public ResponseEntity<FundingRequestResponseDto> rejectFundingRequest(
            @PathVariable UUID fundingRequestId,
            @RequestParam(required = false) String rejectionReason,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        FundingRequest fundingRequest = fundingRequestService.rejectFundingRequest(fundingRequestId, rejectionReason, token);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @GetMapping("/parks/{parkId}/funding-requests")
    public ResponseEntity<List<FundingRequestResponseDto>> getFundingRequestsByPark(@PathVariable UUID parkId) {
        List<FundingRequest> requests = fundingRequestService.getFundingRequestsByPark(parkId);
        return ResponseEntity.ok(requests.stream().map(this::mapToFundingRequestDto).collect(Collectors.toList()));
    }

    private FundingRequestResponseDto mapToFundingRequestDto(FundingRequest request) {
        return new FundingRequestResponseDto(
                request.getId(), request.getPark().getId(), request.getBudget().getId(),
                request.getRequestedAmount(), request.getApprovedAmount(), request.getRequestType(),
                request.getReason(), request.getRequester().getId(),
                request.getApprover() != null ? request.getApprover().getId() : null,
                request.getStatus(), request.getRejectionReason(), request.getApprovedAt(),
                request.getCurrency(), request.getCreatedAt(), request.getUpdatedAt()
        );
    }

    // Mapping Methods
    private BudgetResponseDto mapToBudgetDto(Budget budget) {
        return new BudgetResponseDto(
                budget.getId(), budget.getPark().getId(), budget.getFiscalYear(), budget.getTotalAmount(),
                budget.getBalance(), budget.getStatus(), budget.getCreatedBy().getId(),
                budget.getApprovedBy() != null ? budget.getApprovedBy().getId() : null,
                budget.getApprovedAt(), budget.getCreatedAt(), budget.getUpdatedAt()
        );
    }

    private BudgetCategoryResponseDto mapToCategoryDto(BudgetCategory category) {
        return new BudgetCategoryResponseDto(
                category.getId(), category.getBudget().getId(), category.getName(), category.getAllocatedAmount(),
                category.getUsedAmount(), category.getBalance(), category.getCreatedAt(), category.getUpdatedAt()
        );
    }

    private ExpenseResponseDto mapToExpenseDto(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(), expense.getAmount(), expense.getDescription(), expense.getCategory(),
                expense.getBudgetCategory().getId(), expense.getPark().getId(), expense.getCreatedBy().getId(),
                expense.getStatus(), expense.getApprovedBy() != null ? expense.getApprovedBy().getId() : null,
                expense.getApprovedAt(), expense.getReceiptUrl(), expense.getCurrency(),
                expense.getCreatedAt(), expense.getUpdatedAt()
        );
    }

    private WithdrawRequestResponseDto mapToWithdrawRequestDto(WithdrawRequest request) {
        return new WithdrawRequestResponseDto(
                request.getId(), request.getAmount(), request.getReason(), request.getDescription(),
                request.getRequester().getId(), request.getApprover() != null ? request.getApprover().getId() : null,
                request.getBudgetCategory().getId(), request.getStatus(), request.getApprovedAt(),
                request.getRejectionReason(), request.getPark().getId(), request.getCurrency(),
                request.getCreatedAt(), request.getUpdatedAt()
        );
    }
}