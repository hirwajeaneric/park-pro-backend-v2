package com.park.parkpro.service;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.CreateWithdrawRequestDto;
import com.park.parkpro.dto.UpdateWithdrawRequestDto;
import com.park.parkpro.dto.UpdateAuditStatusDto;
import com.park.parkpro.exception.BadRequestException;
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
public class WithdrawRequestService {
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ParkRepository parkRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public WithdrawRequestService(WithdrawRequestRepository withdrawRequestRepository, BudgetRepository budgetRepository,
                                  BudgetCategoryRepository budgetCategoryRepository, ParkRepository parkRepository, AuditLogRepository auditLogRepository,
                                  UserRepository userRepository, JwtUtil jwtUtil) {
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.budgetRepository = budgetRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.parkRepository = parkRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public WithdrawRequest createWithdrawRequest(UUID budgetId, CreateWithdrawRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(requester.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can create withdraw requests");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Withdraw requests can only be added to APPROVED budgets");
        }

        BudgetCategory budgetCategory = budgetCategoryRepository.findById(request.getBudgetCategoryId())
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + request.getBudgetCategoryId()));
        if (!budgetCategory.getBudget().getId().equals(budgetId)) {
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

        WithdrawRequest withdrawRequest = new WithdrawRequest(
                request.getAmount(), request.getReason(), request.getDescription(), requester,
                budgetCategory, budget, request.getReceiptUrl(), WithdrawRequest.WithdrawRequestStatus.PENDING, park);
        return withdrawRequestRepository.save(withdrawRequest);
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
        if (!"APPROVED".equals(request.getBudget().getStatus())) {
            throw new BadRequestException("Withdraw requests can only be approved for APPROVED budgets");
        }
        if (!WithdrawRequest.WithdrawRequestStatus.PENDING.equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING withdraw requests can be approved");
        }
        if (request.getBudgetCategory().getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient balance in budget category");
        }

        request.setStatus(WithdrawRequest.WithdrawRequestStatus.APPROVED);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        return withdrawRequestRepository.save(request); // Triggers update_withdraw_balances
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
        if (!"APPROVED".equals(request.getBudget().getStatus())) {
            throw new BadRequestException("Withdraw requests can only be rejected for APPROVED budgets");
        }
        if (!WithdrawRequest.WithdrawRequestStatus.PENDING.equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING withdraw requests can be rejected");
        }

        request.setStatus(WithdrawRequest.WithdrawRequestStatus.REJECTED);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setRejectionReason(rejectionReason);
        return withdrawRequestRepository.save(request);
    }

    @Transactional
    public WithdrawRequest updateWithdrawRequest(UUID withdrawRequestId, UpdateWithdrawRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "FINANCE_OFFICER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER, FINANCE_OFFICER, or ADMIN can update withdraw requests");
        }

        WithdrawRequest withdrawRequest = withdrawRequestRepository.findById(withdrawRequestId)
                .orElseThrow(() -> new NotFoundException("Withdraw request not found with ID: " + withdrawRequestId));

        // Update fields
        withdrawRequest.setReason(request.getReason());
        if (request.getDescription() != null) {
            withdrawRequest.setDescription(request.getDescription());
        }
        if (request.getReceiptUrl() != null) {
            withdrawRequest.setReceiptUrl(request.getReceiptUrl());
        }
        if (request.getAmount() != null) {
            if (!WithdrawRequest.WithdrawRequestStatus.PENDING.equals(withdrawRequest.getStatus())) {
                throw new BadRequestException("Amount can only be updated for PENDING withdraw requests");
            }
            if (withdrawRequest.getBudgetCategory().getBalance().compareTo(request.getAmount()) < 0) {
                throw new BadRequestException("Insufficient balance in budget category for new amount");
            }
            withdrawRequest.setAmount(request.getAmount());
        }

        withdrawRequest.setUpdatedAt(LocalDateTime.now());
        return withdrawRequestRepository.save(withdrawRequest); // Triggers update_withdraw_balances if amount changes
    }

    @Transactional
    public WithdrawRequest updateAuditStatus(UUID withdrawRequestId, UpdateAuditStatusDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only AUDITOR can update audit status");
        }

        WithdrawRequest withdrawRequest = withdrawRequestRepository.findById(withdrawRequestId)
                .orElseThrow(() -> new NotFoundException("Withdraw request not found with ID: " + withdrawRequestId));

        String oldAuditStatus = String.valueOf(withdrawRequest.getAuditStatus());
        withdrawRequest.setAuditStatus(request.getAuditStatus());
        withdrawRequest.setUpdatedAt(LocalDateTime.now());
        withdrawRequest = withdrawRequestRepository.save(withdrawRequest);
        // Log to audit_log
        // Note: The database trigger will log the audit_status change, but we set performed_by here
        auditLogRepository.save(new AuditLog(
                "UPDATE_AUDIT_STATUS",
                "WITHDRAW_REQUEST",
                withdrawRequestId,
                "Changed audit_status from " + oldAuditStatus + " to " + request.getAuditStatus(),
                user,
                LocalDateTime.now()
        ));
        return withdrawRequest;
    }

    @Transactional
    public void deleteWithdrawRequest(UUID withdrawRequestId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        WithdrawRequest request = withdrawRequestRepository.findById(withdrawRequestId)
                .orElseThrow(() -> new NotFoundException("Withdraw request not found with ID: " + withdrawRequestId));

        if (WithdrawRequest.WithdrawRequestStatus.APPROVED.equals(request.getStatus()) && !List.of("FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can delete APPROVED withdraw requests");
        }
        if (!List.of("PARK_MANAGER", "FINANCE_OFFICER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER, FINANCE_OFFICER, or ADMIN can delete withdraw requests");
        }

        withdrawRequestRepository.delete(request); // Triggers restore_withdraw_balances if APPROVED
    }

    public List<WithdrawRequest> getWithdrawRequestsByBudgetCategory(UUID budgetCategoryId) {
        BudgetCategory category = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        return withdrawRequestRepository.findByBudgetCategoryId(budgetCategoryId);
    }

    public List<WithdrawRequest> getWithdrawRequestsByBudget(UUID budgetId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only authorized roles can view withdraw requests");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        return withdrawRequestRepository.findByBudgetId(budgetId);
    }

    public List<WithdrawRequest> getWithdrawRequestsByRequester(UUID budgetId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        return withdrawRequestRepository.findByRequesterIdAndBudgetId(user.getId(), budgetId);
    }

    public WithdrawRequest getWithdrawRequestById(UUID withdrawRequestId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR", "PARK_MANAGER").contains(user.getRole())) {
            throw new ForbiddenException("Only authorized roles can view withdraw request details");
        }

        return withdrawRequestRepository.findById(withdrawRequestId)
                .orElseThrow(() -> new NotFoundException("Withdraw request not found with ID: " + withdrawRequestId));
    }
}