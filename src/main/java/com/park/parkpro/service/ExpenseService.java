package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.domain.Expense;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateExpenseRequestDto;
import com.park.parkpro.dto.UpdateExpenseRequestDto;
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
    public Expense createExpense(CreateExpenseRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "PARK_MANAGER", "ADMIN").contains(createdBy.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER, PARK_MANAGER, or ADMIN can create expenses");
        }

        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + request.getBudgetId()));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Expenses can only be added to APPROVED budgets");
        }

        BudgetCategory budgetCategory = budgetCategoryRepository.findById(request.getBudgetCategoryId())
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + request.getBudgetCategoryId()));
        if (!budgetCategory.getBudget().getId().equals(request.getBudgetId())) {
            throw new BadRequestException("Budget category does not belong to the specified budget");
        }
        if (budgetCategory.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance in budget category");
        }

        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + request.getParkId()));
        if (!park.getId().equals(budget.getPark().getId())) {
            throw new BadRequestException("Park does not match the budget's park");
        }

        Expense expense = new Expense(budget, request.getAmount(), request.getDescription(),
                budgetCategory, park, createdBy, Expense.AuditStatus.UNJUSTIFIED);
        if (request.getReceiptUrl() != null) {
            expense.setReceiptUrl(request.getReceiptUrl());
        }
        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByBudgetCategory(UUID budgetCategoryId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "PARK_MANAGER", "ADMIN", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only authorized roles can view expenses");
        }

        BudgetCategory category = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        return expenseRepository.findByBudgetCategoryId(budgetCategoryId);
    }

    public List<Expense> getExpensesByPark(UUID parkId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "PARK_MANAGER", "ADMIN", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only authorized roles can view expenses");
        }

        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
        return expenseRepository.findByParkId(parkId);
    }

    public List<Expense> getExpensesByBudget(UUID budgetId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "PARK_MANAGER", "ADMIN", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only authorized roles can view expenses");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        return expenseRepository.findByBudgetId(budgetId);
    }

    public Expense getExpenseById(UUID expenseId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "PARK_MANAGER", "ADMIN", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only authorized roles can view expense details");
        }

        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));
    }

    @Transactional
    public Expense updateExpense(UUID expenseId, UpdateExpenseRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));

        if ("PARK_MANAGER".equals(user.getRole())) {
            // Park Manager can update: description, receiptUrl, budgetCategory
            if (request.getDescription() != null) {
                expense.setDescription(request.getDescription());
            }
            if (request.getReceiptUrl() != null) {
                expense.setReceiptUrl(request.getReceiptUrl());
            }
            if (request.getBudgetCategoryId() != null) {
                BudgetCategory newCategory = budgetCategoryRepository.findById(request.getBudgetCategoryId())
                        .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + request.getBudgetCategoryId()));
                if (!newCategory.getBudget().getId().equals(expense.getBudget().getId())) {
                    throw new BadRequestException("New budget category must belong to the same budget");
                }
                if (newCategory.getBalance().compareTo(expense.getAmount()) < 0) {
                    throw new BadRequestException("Insufficient balance in new budget category");
                }
                // Revert old category balance
                BudgetCategory oldCategory = expense.getBudgetCategory();
                oldCategory.setUsedAmount(oldCategory.getUsedAmount().subtract(expense.getAmount()));
                oldCategory.setBalance(oldCategory.getAllocatedAmount().subtract(oldCategory.getUsedAmount()));
                budgetCategoryRepository.save(oldCategory);
                // Update to new category
                expense.setBudgetCategory(newCategory);
                newCategory.setUsedAmount(newCategory.getUsedAmount().add(expense.getAmount()));
                newCategory.setBalance(newCategory.getAllocatedAmount().subtract(newCategory.getUsedAmount()));
                budgetCategoryRepository.save(newCategory);
            }
        } else if ("FINANCE_OFFICER".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
            // Finance Officer can update: amount, auditStatus, budgetCategory
            if (request.getAmount() != null) {
                BigDecimal oldAmount = expense.getAmount();
                BudgetCategory category = expense.getBudgetCategory();
                if (category.getBalance().add(oldAmount).compareTo(request.getAmount()) < 0) {
                    throw new BadRequestException("Insufficient balance in budget category for new amount");
                }
                // Adjust category balance
                category.setUsedAmount(category.getUsedAmount().subtract(oldAmount).add(request.getAmount()));
                category.setBalance(category.getAllocatedAmount().subtract(category.getUsedAmount()));
                budgetCategoryRepository.save(category);
                // Adjust budget balance
                Budget budget = expense.getBudget();
                budget.setBalance(budget.getBalance().add(oldAmount).subtract(request.getAmount()));
                budgetRepository.save(budget);
                expense.setAmount(request.getAmount());
            }
            if (request.getAuditStatus() != null) {
                expense.setAuditStatus(request.getAuditStatus());
            }
            if (request.getBudgetCategoryId() != null) {
                BudgetCategory newCategory = budgetCategoryRepository.findById(request.getBudgetCategoryId())
                        .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + request.getBudgetCategoryId()));
                if (!newCategory.getBudget().getId().equals(expense.getBudget().getId())) {
                    throw new BadRequestException("New budget category must belong to the same budget");
                }
                if (newCategory.getBalance().compareTo(expense.getAmount()) < 0) {
                    throw new BadRequestException("Insufficient balance in new budget category");
                }
                // Revert old category balance
                BudgetCategory oldCategory = expense.getBudgetCategory();
                oldCategory.setUsedAmount(oldCategory.getUsedAmount().subtract(expense.getAmount()));
                oldCategory.setBalance(oldCategory.getAllocatedAmount().subtract(oldCategory.getUsedAmount()));
                budgetCategoryRepository.save(oldCategory);
                // Update to new category
                expense.setBudgetCategory(newCategory);
                newCategory.setUsedAmount(newCategory.getUsedAmount().add(expense.getAmount()));
                newCategory.setBalance(newCategory.getAllocatedAmount().subtract(newCategory.getUsedAmount()));
                budgetCategoryRepository.save(newCategory);
            }
        } else {
            throw new ForbiddenException("User role not authorized to update expenses");
        }

        expense.setUpdatedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }
}