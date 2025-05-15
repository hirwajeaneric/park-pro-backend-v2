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
    public IncomeStream createIncomeStream(UUID budgetId, String name, BigDecimal percentage,
                                           BigDecimal totalContribution, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be added to DRAFT budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"FINANCE_OFFICER".equals(createdBy.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can create income streams");
        }

        Park park = budget.getPark();
        IncomeStream incomeStream = new IncomeStream();
        incomeStream.setBudget(budget);
        incomeStream.setPark(park);
        incomeStream.setFiscalYear(budget.getFiscalYear());
        incomeStream.setName(name);
        incomeStream.setPercentage(percentage);
        incomeStream.setTotalContribution(totalContribution);
        incomeStream.setActualBalance(BigDecimal.ZERO);
        incomeStream.setCreatedBy(createdBy);
        return incomeStreamRepository.save(incomeStream);
    }

    @Transactional
    public IncomeStream updateIncomeStream(UUID incomeStreamId, String name, BigDecimal percentage,
                                           BigDecimal totalContribution, String token) {
        IncomeStream incomeStream = incomeStreamRepository.findById(incomeStreamId)
                .orElseThrow(() -> new NotFoundException("Income stream not found with ID: " + incomeStreamId));
        Budget budget = incomeStream.getBudget();
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be updated for DRAFT budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"FINANCE_OFFICER".equals(user.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can update income streams");
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