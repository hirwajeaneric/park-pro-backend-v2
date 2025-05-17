package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.IncomeStream;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.IncomeStreamRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class IncomeStreamService {
    private final IncomeStreamRepository incomeStreamRepository;
    private final BudgetRepository budgetRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public IncomeStreamService(IncomeStreamRepository incomeStreamRepository,
                               BudgetRepository budgetRepository,
                               ParkRepository parkRepository,
                               UserRepository userRepository,
                               JwtUtil jwtUtil) {
        this.incomeStreamRepository = incomeStreamRepository;
        this.budgetRepository = budgetRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public IncomeStream createIncomeStream(UUID budgetId, String name, BigDecimal percentage, BigDecimal totalContribution, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Restrict modifications to DRAFT budgets
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be modified for DRAFT budgets");
        }

        // Validate percentage
        BigDecimal sumPercentage = incomeStreamRepository.sumPercentageByBudgetId(budgetId)
                .orElse(BigDecimal.ZERO);
        if (sumPercentage.add(percentage).compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Total percentage for budget exceeds 100%");
        }

        // Validate total_contribution
        BigDecimal sumContribution = incomeStreamRepository.sumTotalContributionByBudgetId(budgetId)
                .orElse(BigDecimal.ZERO);
        if (sumContribution.add(totalContribution).compareTo(budget.getTotalAmount()) > 0) {
            throw new BadRequestException("Total contribution exceeds budget total amount");
        }

        IncomeStream incomeStream = new IncomeStream();
        incomeStream.setBudget(budget);
        incomeStream.setName(name);
        incomeStream.setPercentage(percentage);
        incomeStream.setTotalContribution(totalContribution);
        incomeStream.setActualBalance(BigDecimal.ZERO);
        incomeStream.setCreatedBy(user);
        return incomeStreamRepository.save(incomeStream);
    }

    @Transactional
    public IncomeStream updateIncomeStream(UUID incomeStreamId, String name, BigDecimal percentage, BigDecimal totalContribution, String token) {
        IncomeStream incomeStream = incomeStreamRepository.findById(incomeStreamId)
                .orElseThrow(() -> new NotFoundException("Income stream not found with ID: " + incomeStreamId));
        Budget budget = incomeStream.getBudget();

        // Restrict modifications to DRAFT budgets
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be modified for DRAFT budgets");
        }

        // Validate percentage
        BigDecimal sumPercentage = incomeStreamRepository.sumPercentageByBudgetIdExcluding(budget.getId(), incomeStreamId)
                .orElse(BigDecimal.ZERO);
        if (sumPercentage.add(percentage).compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Total percentage for budget exceeds 100%");
        }

        // Validate total_contribution
        BigDecimal sumContribution = incomeStreamRepository.sumTotalContributionByBudgetIdExcluding(budget.getId(), incomeStreamId)
                .orElse(BigDecimal.ZERO);
        if (sumContribution.add(totalContribution).compareTo(budget.getTotalAmount()) > 0) {
            throw new BadRequestException("Total contribution exceeds budget total amount");
        }

        incomeStream.setName(name);
        incomeStream.setPercentage(percentage);
        incomeStream.setTotalContribution(totalContribution);
        return incomeStreamRepository.save(incomeStream);
    }

    public List<IncomeStream> getIncomeStreamsByBudget(UUID budgetId) {
        return incomeStreamRepository.findByBudgetId(budgetId);
    }

    public List<IncomeStream> getIncomeStreamsByParkAndFiscalYear(UUID parkId, Integer fiscalYear) {
        return incomeStreamRepository.findByParkIdAndFiscalYear(parkId, fiscalYear);
    }

    @Transactional
    public void deleteIncomeStream(UUID incomeStreamId, String token) {
        IncomeStream incomeStream = incomeStreamRepository.findById(incomeStreamId)
                .orElseThrow(() -> new NotFoundException("Income stream not found with ID: " + incomeStreamId));
        Budget budget = incomeStream.getBudget();
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be deleted for DRAFT budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"FINANCE_OFFICER".equals(user.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can delete income streams");
        }

        incomeStreamRepository.delete(incomeStream);
    }
}