package com.park.parkpro.service;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.BudgetByFiscalYearResponseDto;
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
import java.util.stream.Collectors;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final IncomeStreamRepository incomeStreamRepository;
    private final JwtUtil jwtUtil;

    public BudgetService(BudgetRepository budgetRepository, ParkRepository parkRepository,
                         UserRepository userRepository, IncomeStreamRepository incomeStreamRepository,
                         JwtUtil jwtUtil) {
        this.budgetRepository = budgetRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.incomeStreamRepository = incomeStreamRepository;
        this.jwtUtil = jwtUtil;
    }

    public Budget getBudgetById(UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with id: " + budgetId));
        // Sync balance with category balances
        budget.setBalance(budgetRepository.sumCategoryBalances(budgetId));
        return budget;
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
        budget.setUnallocated(totalAmount); // Initialize unallocated
        budget.setStatus(status);
        budget.setCreatedBy(createdBy);
        budget.setApprovedBy(null); // Explicitly null for DRAFT
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(UUID budgetId, Integer fiscalYear, BigDecimal totalAmount, String status, String token) {
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
        budget.setFiscalYear(fiscalYear);
        budget.setUnallocated(totalAmount.subtract(budgetRepository.sumCategoryBalances(budgetId))); // Sync unallocated
        budget.setBalance(budgetRepository.sumCategoryBalances(budgetId)); // Sync balance
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
        // Top up government income streams (Rule 8)
        incomeStreamRepository.findByBudgetIdAndNameContaining(budgetId, "Government")
                .forEach(is -> {
                    is.setActualBalance(is.getTotalContribution());
                    incomeStreamRepository.save(is);
                });
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
        return budgetRepository.findByParkId(parkId).stream()
                .peek(budget -> budget.setBalance(budgetRepository.sumCategoryBalances(budget.getId())))
                .collect(Collectors.toList());
    }

    public List<BudgetByFiscalYearResponseDto> getBudgetsByFiscalYear(Integer fiscalYear, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only GOVERNMENT_OFFICER and AUDITOR can view budgets by fiscal year");
        }

        List<Park> allParks = parkRepository.findAll();
        List<Budget> budgets = budgetRepository.findByFiscalYear(fiscalYear);

        return allParks.stream().map(park -> {
            Budget budget = budgets.stream()
                    .filter(b -> b.getPark().getId().equals(park.getId()))
                    .findFirst()
                    .orElse(null);
            return new BudgetByFiscalYearResponseDto(
                    budget != null ? budget.getId() : null,
                    park.getId(),
                    park.getName(),
                    fiscalYear,
                    budget != null ? budget.getTotalAmount() : null,
                    budget != null ? budgetRepository.sumCategoryBalances(budget.getId()) : null,
                    budget != null ? budget.getUnallocated() : null,
                    budget != null ? budget.getStatus() : null,
                    budget != null ? budget.getCreatedBy().getId() : null,
                    budget != null && budget.getApprovedBy() != null ? budget.getApprovedBy().getId() : null,
                    budget != null ? budget.getApprovedAt() : null,
                    budget != null ? budget.getCreatedAt() : null,
                    budget != null ? budget.getUpdatedAt() : null
            );
        }).collect(Collectors.toList());
    }
}