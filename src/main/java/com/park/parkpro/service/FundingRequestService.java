// src/main/java/com/park/parkpro/service/FundingRequestService.java
package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.FundingRequest;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.FundingRequestRepository;
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
public class FundingRequestService {
    private final FundingRequestRepository fundingRequestRepository;
    private final BudgetRepository budgetRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public FundingRequestService(FundingRequestRepository fundingRequestRepository,
                                 BudgetRepository budgetRepository,
                                 ParkRepository parkRepository,
                                 UserRepository userRepository,
                                 JwtUtil jwtUtil) {
        this.fundingRequestRepository = fundingRequestRepository;
        this.budgetRepository = budgetRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public FundingRequest createFundingRequest(UUID parkId, BigDecimal requestedAmount, String requestType,
                                               String reason, UUID budgetId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"FINANCE_OFFICER".equals(requester.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can create funding requests");
        }
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
        if (!park.getId().equals(requester.getPark().getId())) {
            throw new ForbiddenException("You can only request funds for your assigned park");
        }
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Funding requests can only be made for APPROVED budgets");
        }
        if (!budget.getPark().getId().equals(parkId)) {
            throw new BadRequestException("Budget does not belong to the specified park");
        }
        if (!List.of("EXTRA_FUNDS", "EMERGENCY_RELIEF").contains(requestType)) {
            throw new BadRequestException("Invalid request type: " + requestType);
        }

        FundingRequest request = new FundingRequest();
        request.setPark(park);
        request.setBudget(budget);
        request.setRequestedAmount(requestedAmount);
        request.setRequestType(requestType);
        request.setReason(reason);
        request.setRequester(requester);
        request.setStatus("PENDING");
        return fundingRequestRepository.save(request);
    }

    @Transactional
    public FundingRequest approveFundingRequest(UUID fundingRequestId, BigDecimal approvedAmount, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"GOVERNMENT_OFFICER".equals(approver.getRole())) {
            throw new ForbiddenException("Only GOVERNMENT_OFFICER can approve funding requests");
        }
        FundingRequest request = fundingRequestRepository.findById(fundingRequestId)
                .orElseThrow(() -> new NotFoundException("Funding request not found with ID: " + fundingRequestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING funding requests can be approved");
        }
        if (approvedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Approved amount cannot be negative");
        }

        request.setStatus("APPROVED");
        request.setApprovedAmount(approvedAmount);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        return fundingRequestRepository.save(request); // Trigger updates budget balance
    }

    @Transactional
    public FundingRequest rejectFundingRequest(UUID fundingRequestId, String rejectionReason, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User approver = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"GOVERNMENT_OFFICER".equals(approver.getRole())) {
            throw new ForbiddenException("Only GOVERNMENT_OFFICER can reject funding requests");
        }
        FundingRequest request = fundingRequestRepository.findById(fundingRequestId)
                .orElseThrow(() -> new NotFoundException("Funding request not found with ID: " + fundingRequestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING funding requests can be rejected");
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(rejectionReason);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        return fundingRequestRepository.save(request);
    }

    public List<FundingRequest> getFundingRequestsByPark(UUID parkId) {
        return fundingRequestRepository.findByParkId(parkId);
    }
}