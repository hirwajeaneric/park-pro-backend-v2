package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.repository.BudgetRepository;
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
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public BudgetService(BudgetRepository budgetRepository, ParkRepository parkRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.budgetRepository = budgetRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Budget createBudget(UUID parkId, Integer fiscalYear, BigDecimal totalAmount, String status, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("Park not found: " + parkId));

        if (budgetRepository.findByParkIdAndFiscalYear(parkId, fiscalYear).isPresent()) {
            throw new IllegalArgumentException("Budget already exists for park " + parkId + " and fiscal year " + fiscalYear);
        }

        Budget budget = new Budget();
        budget.setPark(park);
        budget.setFiscalYear(fiscalYear);
        budget.setTotalAmount(totalAmount);
        budget.setBalance(totalAmount); // Initial balance = total amount
        budget.setStatus(status);
        budget.setCreatedBy(createdBy);
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(UUID budgetId, BigDecimal totalAmount, String status, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new IllegalStateException("Only DRAFT budgets can be updated");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        budget.setTotalAmount(totalAmount);
        budget.setBalance(totalAmount); // Reset balance (no expenses yet in this scope)
        budget.setStatus(status);
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget approveBudget(UUID budgetId, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new IllegalStateException("Only DRAFT budgets can be approved");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        if (!"GOVERNMENT_OFFICER".equals(approver.getRole())) {
            throw new IllegalStateException("Only GOVERNMENT_OFFICER can approve budgets");
        }

        budget.setStatus("APPROVED");
        budget.setApprovedBy(approver);
        budget.setApprovedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget rejectBudget(UUID budgetId, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new IllegalStateException("Only DRAFT budgets can be rejected");
        }
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        if (!"GOVERNMENT_OFFICER".equals(approver.getRole())) {
            throw new IllegalStateException("Only GOVERNMENT_OFFICER can reject budgets");
        }

        budget.setStatus("REJECTED");
        budget.setApprovedBy(approver);
        budget.setApprovedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsByPark(UUID parkId) {
        return budgetRepository.findByParkId(parkId);
    }
}