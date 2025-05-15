package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetCategoryRepository;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetCategoryService {
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public BudgetCategoryService(BudgetCategoryRepository budgetCategoryRepository,
                                 BudgetRepository budgetRepository,
                                 UserRepository userRepository,
                                 JwtUtil jwtUtil) {
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public BudgetCategory createBudgetCategory(UUID budgetId, String name, BigDecimal percentage, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Categories can only be added to DRAFT budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or FINANCE_OFFICER can create budget categories");
        }

        // Calculate allocated amount
        BigDecimal allocatedAmount = budget.getBalance().multiply(percentage.divide(BigDecimal.valueOf(100)));
        // Ensure unallocated is initialized correctly
        BigDecimal unallocated = budget.getUnallocated() != null ? budget.getUnallocated() : budget.getBalance();
        if (allocatedAmount.compareTo(unallocated) > 0) {
            throw new BadRequestException("Insufficient unallocated funds: " + unallocated + " available, " + allocatedAmount + " required");
        }

        BudgetCategory category = new BudgetCategory();
        category.setBudget(budget);
        category.setName(name);
        category.setAllocatedAmount(allocatedAmount);
        category.setUsedAmount(BigDecimal.ZERO);
        category.setBalance(allocatedAmount);
        return budgetCategoryRepository.save(category);
    }

    @Transactional
    public BudgetCategory updateBudgetCategory(UUID categoryId, BigDecimal allocatedAmount, String token) {
        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + categoryId));
        Budget budget = category.getBudget();
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Categories can only be updated for DRAFT budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or FINANCE_OFFICER can update budget categories");
        }

        BigDecimal oldAllocated = category.getAllocatedAmount();
        BigDecimal unallocated = budget.getUnallocated();
        if (allocatedAmount.compareTo(oldAllocated) > 0) {
            BigDecimal additional = allocatedAmount.subtract(oldAllocated);
            if (additional.compareTo(unallocated) > 0) {
                throw new BadRequestException("Insufficient unallocated funds for category allocation");
            }
        }

        category.setAllocatedAmount(allocatedAmount);
        category.setBalance(allocatedAmount.subtract(category.getUsedAmount()));
        return budgetCategoryRepository.save(category);
    }

    public List<BudgetCategory> getBudgetCategoriesByBudget(UUID budgetId) {
        return budgetCategoryRepository.findByBudgetId(budgetId);
    }

    @Transactional
    public void deleteBudgetCategory(UUID categoryId, String token) {
        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + categoryId));
        Budget budget = category.getBudget();
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Categories can only be deleted for DRAFT budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or FINANCE_OFFICER can delete budget categories");
        }

        budgetCategoryRepository.delete(category);
    }
}