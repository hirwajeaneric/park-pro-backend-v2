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
    public BudgetCategory createBudgetCategory(UUID budgetId, String name, BigDecimal allocatedAmount, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or FINANCE_OFFICER can create budget categories");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Categories can only be added to APPROVED budgets");
        }
        if (budget.getBalance().compareTo(allocatedAmount) < 0) {
            throw new BadRequestException("Allocated amount exceeds remaining budget balance");
        }

        BudgetCategory category = new BudgetCategory(budget, name, allocatedAmount);
        budget.setBalance(budget.getBalance().subtract(allocatedAmount));
        budgetRepository.save(budget);
        return budgetCategoryRepository.save(category);
    }

    public List<BudgetCategory> getCategoriesByBudget(UUID budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new NotFoundException("Budget not found with ID: " + budgetId);
        }
        return budgetCategoryRepository.findByBudgetId(budgetId);
    }

    public BudgetCategory getCategoryById(UUID categoryId) {
        return budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + categoryId));
    }

    @Transactional
    public BudgetCategory updateBudgetCategory(UUID categoryId, String name, BigDecimal allocatedAmount, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or FINANCE_OFFICER can update budget categories");
        }

        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + categoryId));
        Budget budget = category.getBudget();
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Only categories in DRAFT budgets can be updated");
        }

        BigDecimal oldAllocatedAmount = category.getAllocatedAmount();
        if (allocatedAmount.compareTo(oldAllocatedAmount) != 0) {
            BigDecimal difference = allocatedAmount.subtract(oldAllocatedAmount);
            if (budget.getBalance().compareTo(difference) < 0) {
                throw new BadRequestException("New allocated amount exceeds remaining budget balance");
            }
            budget.setBalance(budget.getBalance().subtract(difference));
            category.setAllocatedAmount(allocatedAmount);
            category.setBalance(allocatedAmount.subtract(category.getUsedAmount()));
            budgetRepository.save(budget);
        }
        if (!name.equals(category.getName())) {
            category.setName(name);
        }
        return budgetCategoryRepository.save(category);
    }

    @Transactional
    public void deleteBudgetCategory(UUID categoryId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN or FINANCE_OFFICER can delete budget categories");
        }

        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + categoryId));
        if (budgetCategoryRepository.existsByIdAndUsedAmountGreaterThan(categoryId, BigDecimal.ZERO)) {
            throw new BadRequestException("Cannot delete category with used amount greater than zero");
        }

        Budget budget = category.getBudget();
        budget.setBalance(budget.getBalance().add(category.getAllocatedAmount()));
        budgetRepository.save(budget);
        budgetCategoryRepository.delete(category);
    }
}