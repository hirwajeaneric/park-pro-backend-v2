package com.park.parkpro.service;

import com.park.parkpro.domain.*;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ConflictException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.*;
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

    public Budget getBudgetById(UUID budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with id: "));
    }

    @Transactional
    public Budget createBudget(UUID parkId, Integer fiscalYear, BigDecimal totalAmount, String status, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Total amount cannot be negative");
        }
        if (!"DRAFT".equals(status)) {
            throw new BadRequestException("New budgets must start as DRAFT");
        }
        if (budgetRepository.findByParkIdAndFiscalYear(parkId, fiscalYear).isPresent()) {
            throw new ConflictException("A budget already exists for park " + parkId + " and fiscal year " + fiscalYear);
        }

        Budget budget = new Budget();
        budget.setPark(park);
        budget.setFiscalYear(fiscalYear);
        budget.setTotalAmount(totalAmount);
        budget.setBalance(totalAmount);
        budget.setStatus(status);
        budget.setCreatedBy(createdBy);
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(UUID budgetId, BigDecimal totalAmount, String status, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Only DRAFT budgets can be updated");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Total amount cannot be negative");
        }
        if (!"DRAFT".equals(status)) {
            throw new BadRequestException("Updated budgets must remain in DRAFT status");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        budget.setTotalAmount(totalAmount);
        budget.setBalance(totalAmount); // Reset balance since no expenses yet
        budget.setStatus(status);
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget approveBudget(UUID budgetId, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Only DRAFT budgets can be approved");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"GOVERNMENT_OFFICER".equals(approver.getRole())) {
            throw new ForbiddenException("Only GOVERNMENT_OFFICER can approve budgets");
        }

        budget.setStatus("APPROVED");
        budget.setApprovedBy(approver);
        budget.setApprovedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget rejectBudget(UUID budgetId, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Only DRAFT budgets can be rejected");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"GOVERNMENT_OFFICER".equals(approver.getRole())) {
            throw new ForbiddenException("Only GOVERNMENT_OFFICER can reject budgets");
        }

        budget.setStatus("REJECTED");
        budget.setApprovedBy(approver);
        budget.setApprovedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsByPark(UUID parkId) {
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park not found with ID: " + parkId);
        }
        return budgetRepository.findByParkId(parkId);
    }
}