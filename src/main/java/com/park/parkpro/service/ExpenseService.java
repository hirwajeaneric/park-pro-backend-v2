package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.domain.Expense;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.BudgetCategoryRepository;
import com.park.parkpro.repository.ExpenseRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ExpenseService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository,
                          BudgetCategoryRepository budgetCategoryRepository, ParkRepository parkRepository,
                          UserRepository userRepository, JwtUtil jwtUtil) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Expense createExpense(UUID budgetId, BigDecimal amount, String description, String category,
                                 UUID budgetCategoryId, UUID parkId, String receiptUrl, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Expenses can only be added to APPROVED budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "PARK_MANAGER", "ADMIN").contains(createdBy.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER, PARK_MANAGER, or ADMIN can create expenses");
        }

        BudgetCategory budgetCategory = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        if (!budgetCategory.getBudget().getId().equals(budgetId)) {
            throw new BadRequestException("Budget category does not belong to the specified budget");
        }
        if (budgetCategory.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance in budget category");
        }

        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
        if (!park.getId().equals(budget.getPark().getId())) {
            throw new BadRequestException("Park does not match the budget's park");
        }

        Expense expense = new Expense(amount, description, category, budgetCategory, park, createdBy, "PENDING");
        if (receiptUrl != null) {
            expense.setReceiptUrl(receiptUrl);
        }
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense approveExpense(UUID expenseId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(approver.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can approve expenses");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));
        if (!"APPROVED".equals(expense.getBudgetCategory().getBudget().getStatus())) {
            throw new BadRequestException("Expenses can only be approved for APPROVED budgets");
        }
        if (!"PENDING".equals(expense.getStatus())) {
            throw new BadRequestException("Only PENDING expenses can be approved");
        }
        if (expense.getBudgetCategory().getBalance().compareTo(expense.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance in budget category");
        }

        expense.setStatus("APPROVED");
        expense.setApprovedBy(approver);
        expense.setApprovedAt(LocalDateTime.now());
        return expenseRepository.save(expense); // Triggers update_category_balance and update_budget_balance
    }

    @Transactional
    public Expense rejectExpense(UUID expenseId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(approver.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can reject expenses");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));
        if (!"APPROVED".equals(expense.getBudgetCategory().getBudget().getStatus())) {
            throw new BadRequestException("Expenses can only be rejected for APPROVED budgets");
        }
        if (!"PENDING".equals(expense.getStatus())) {
            throw new BadRequestException("Only PENDING expenses can be rejected");
        }

        expense.setStatus("REJECTED");
        expense.setApprovedBy(approver);
        expense.setApprovedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByBudgetCategory(UUID budgetCategoryId) {
        BudgetCategory category = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        return expenseRepository.findByBudgetCategoryId(budgetCategoryId);
    }
}