package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.domain.WithdrawRequest;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.BudgetCategoryRepository;
import com.park.parkpro.repository.WithdrawRequestRepository;
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
public class WithdrawRequestService {
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public WithdrawRequestService(WithdrawRequestRepository withdrawRequestRepository, BudgetRepository budgetRepository,
                                  BudgetCategoryRepository budgetCategoryRepository, ParkRepository parkRepository,
                                  UserRepository userRepository, JwtUtil jwtUtil) {
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.budgetRepository = budgetRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public WithdrawRequest createWithdrawRequest(UUID budgetId, BigDecimal amount, String reason, String description,
                                                 UUID budgetCategoryId, UUID parkId, String token) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Withdraw requests can only be added to APPROVED budgets");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(requester.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can create withdraw requests");
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

        WithdrawRequest request = new WithdrawRequest(amount, reason, description, requester, budgetCategory, "PENDING", park);
        return withdrawRequestRepository.save(request);
    }

    @Transactional
    public WithdrawRequest approveWithdrawRequest(UUID withdrawRequestId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(approver.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can approve withdraw requests");
        }

        WithdrawRequest request = withdrawRequestRepository.findById(withdrawRequestId)
                .orElseThrow(() -> new NotFoundException("Withdraw request not found with ID: " + withdrawRequestId));
        if (!"APPROVED".equals(request.getBudgetCategory().getBudget().getStatus())) {
            throw new BadRequestException("Withdraw requests can only be approved for APPROVED budgets");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING withdraw requests can be approved");
        }
        if (request.getBudgetCategory().getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance in budget category");
        }

        request.setStatus("APPROVED");
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        return withdrawRequestRepository.save(request); // Triggers update_category_balance and update_budget_balance
    }

    @Transactional
    public WithdrawRequest rejectWithdrawRequest(UUID withdrawRequestId, String rejectionReason, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(approver.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can reject withdraw requests");
        }

        WithdrawRequest request = withdrawRequestRepository.findById(withdrawRequestId)
                .orElseThrow(() -> new NotFoundException("Withdraw request not found with ID: " + withdrawRequestId));
        if (!"APPROVED".equals(request.getBudgetCategory().getBudget().getStatus())) {
            throw new BadRequestException("Withdraw requests can only be rejected for APPROVED budgets");
        }
        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING withdraw requests can be rejected");
        }

        request.setStatus("REJECTED");
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setRejectionReason(rejectionReason);
        return withdrawRequestRepository.save(request);
    }

    public List<WithdrawRequest> getWithdrawRequestsByBudgetCategory(UUID budgetCategoryId) {
        BudgetCategory category = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        return withdrawRequestRepository.findByBudgetCategoryId(budgetCategoryId);
    }
}