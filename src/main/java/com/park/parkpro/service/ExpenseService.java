package com.park.parkpro.service;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.CreateExpenseRequestDto;
import com.park.parkpro.dto.UpdateAuditStatusDto;
import com.park.parkpro.dto.UpdateExpenseRequestDto;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.*;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ParkRepository parkRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ExpenseService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository,
                          BudgetCategoryRepository budgetCategoryRepository, ParkRepository parkRepository, AuditLogRepository auditLogRepository,
                          UserRepository userRepository, JwtUtil jwtUtil) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.parkRepository = parkRepository;
        this.auditLogRepository = auditLogRepository;
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

        // New validation for monthly expense limits
        Month currentMonth = LocalDateTime.now().getMonth();
        if (currentMonth.getValue() >= Month.JULY.getValue()) {
            BigDecimal allocatedAmount = budgetCategory.getAllocatedAmount();
            BigDecimal currentBalance = budgetCategory.getBalance();
            BigDecimal sevenTwelfths = allocatedAmount.multiply(new BigDecimal("7")).divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            BigDecimal twoTwelfths = allocatedAmount.multiply(new BigDecimal("2")).divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

            if (currentBalance.compareTo(sevenTwelfths) < 0 && request.getAmount().compareTo(twoTwelfths) >= 0) {
                throw new BadRequestException(
                    "Cannot withdraw more than 2/12 of allocated amount when balance is below 7/12 of allocated amount after July"
                );
            }
        }

        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + request.getParkId()));
        if (!park.getId().equals(budget.getPark().getId())) {
            throw new BadRequestException("Park does not match the budget's park");
        }

        // Update budget and category balances
        budgetCategory.setUsedAmount(budgetCategory.getUsedAmount().add(request.getAmount()));
        budgetCategory.setBalance(budgetCategory.getAllocatedAmount().subtract(budgetCategory.getUsedAmount()));
        budgetCategoryRepository.save(budgetCategory);

        budget.setBalance(budget.getBalance().subtract(request.getAmount()));
        budgetRepository.save(budget);

        Expense expense = new Expense(budget, request.getAmount(), request.getDescription(),
                budgetCategory, park, createdBy, AuditStatus.UNJUSTIFIED);
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

    public List<Expense> getExpensesByCreatedBy(UUID budgetId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));

        return expenseRepository.findByCreatedByIdAndBudgetId(user.getId(), budgetId);
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
                BudgetCategory oldCategory = expense.getBudgetCategory();
                oldCategory.setUsedAmount(oldCategory.getUsedAmount().subtract(expense.getAmount()));
                oldCategory.setBalance(oldCategory.getAllocatedAmount().subtract(oldCategory.getUsedAmount()));
                budgetCategoryRepository.save(oldCategory);
                expense.setBudgetCategory(newCategory);
                newCategory.setUsedAmount(newCategory.getUsedAmount().add(expense.getAmount()));
                newCategory.setBalance(newCategory.getAllocatedAmount().subtract(newCategory.getUsedAmount()));
                budgetCategoryRepository.save(newCategory);
            }
        } else if ("FINANCE_OFFICER".equals(user.getRole()) || "ADMIN".equals(user.getRole())) {
            if (request.getAmount() != null) {
                BigDecimal oldAmount = expense.getAmount();
                BudgetCategory category = expense.getBudgetCategory();
                if (category.getBalance().add(oldAmount).compareTo(request.getAmount()) < 0) {
                    throw new BadRequestException("Insufficient balance in budget category for new amount");
                }
                category.setUsedAmount(category.getUsedAmount().subtract(oldAmount).add(request.getAmount()));
                category.setBalance(category.getAllocatedAmount().subtract(category.getUsedAmount()));
                budgetCategoryRepository.save(category);
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
                BudgetCategory oldCategory = expense.getBudgetCategory();
                oldCategory.setUsedAmount(oldCategory.getUsedAmount().subtract(expense.getAmount()));
                oldCategory.setBalance(oldCategory.getAllocatedAmount().subtract(oldCategory.getUsedAmount()));
                budgetCategoryRepository.save(oldCategory);
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

    @Transactional
    public void deleteExpense(UUID expenseId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can delete expenses");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));

        expenseRepository.delete(expense);
    }

    @Transactional
    public Expense updateAuditStatus(UUID expenseId, UpdateAuditStatusDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only AUDITOR can update audit status");
        }

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));

        request.validate(); // Validate justification requirements

        String oldAuditStatus = String.valueOf(expense.getAuditStatus());
        expense.setAuditStatus(request.getAuditStatus());
        expense.setJustification(request.getJustification());
        expense.setUpdatedAt(LocalDateTime.now());
        expense = expenseRepository.save(expense);

        if (!oldAuditStatus.equals(request.getAuditStatus().toString())) {
            String logMessage = String.format(
                    "Changed audit_status from %s to %s%s",
                    oldAuditStatus,
                    request.getAuditStatus(),
                    request.getJustification() != null ? " with justification: " + request.getJustification() : ""
            );
            auditLogRepository.save(new AuditLog(
                    "UPDATE_AUDIT_STATUS",
                    "EXPENSE",
                    expenseId,
                    logMessage,
                    user,
                    LocalDateTime.now()
            ));
        }

        return expense;
    }
}