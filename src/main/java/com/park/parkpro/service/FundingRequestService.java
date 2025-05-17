package com.park.parkpro.service;

import com.park.parkpro.domain.Budget;
import com.park.parkpro.domain.BudgetCategory;
import com.park.parkpro.domain.FundingRequest;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetCategoryRepository;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.FundingRequestRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class FundingRequestService {
    private static final Logger LOGGER = Logger.getLogger(FundingRequestService.class.getName());
    private final FundingRequestRepository fundingRequestRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public FundingRequestService(FundingRequestRepository fundingRequestRepository,
                                 BudgetRepository budgetRepository,
                                 BudgetCategoryRepository budgetCategoryRepository,
                                 ParkRepository parkRepository,
                                 UserRepository userRepository,
                                 JwtUtil jwtUtil) {
        this.fundingRequestRepository = fundingRequestRepository;
        this.budgetRepository = budgetRepository;
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public FundingRequest createFundingRequest(UUID parkId, BigDecimal requestedAmount, String requestType,
                                               String reason, UUID budgetId, UUID budgetCategoryId, String token) {
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
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        if (!budgetCategory.getBudget().getId().equals(budgetId)) {
            throw new BadRequestException("Budget category does not belong to the specified budget");
        }
        if (!List.of("EXTRA_FUNDS", "EMERGENCY_RELIEF").contains(requestType)) {
            throw new BadRequestException("Invalid request type: " + requestType);
        }
        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Requested amount must be positive");
        }

        FundingRequest request = new FundingRequest();
        request.setPark(park);
        request.setBudget(budget);
        request.setBudgetCategory(budgetCategory);
        request.setRequestedAmount(requestedAmount);
        request.setRequestType(requestType);
        request.setReason(reason);
        request.setRequester(requester);
        request.setStatus("PENDING");
        request.setCurrency("XAF");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        FundingRequest savedRequest = fundingRequestRepository.save(request);
        LOGGER.info("Created funding request: ID=" + savedRequest.getId());
        return savedRequest;
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

        Budget budget = request.getBudget();
        BudgetCategory category = request.getBudgetCategory();
        BigDecimal unallocated = budget.getUnallocated();
        if (approvedAmount.compareTo(unallocated) > 0) {
            throw new BadRequestException("Insufficient unallocated funds: " + unallocated);
        }

        // Update budget and category (Rule 6)
        budget.setTotalAmount(budget.getTotalAmount().add(approvedAmount));
        budget.setUnallocated(unallocated.subtract(approvedAmount));
        category.setAllocatedAmount(category.getAllocatedAmount().add(approvedAmount));
        category.setBalance(category.getBalance().add(approvedAmount));
        budget.setBalance(budgetRepository.sumCategoryBalances(budget.getId()));
        budget.setUpdatedAt(LocalDateTime.now());

        budgetCategoryRepository.save(category);
        budgetRepository.save(budget);

        request.setStatus("APPROVED");
        request.setApprovedAmount(approvedAmount);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        FundingRequest savedRequest = fundingRequestRepository.save(request);
        LOGGER.info("Approved funding request: ID=" + savedRequest.getId() + ", Approved Amount=" + approvedAmount);
        return savedRequest;
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
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new BadRequestException("Rejection reason must be provided");
        }

        request.setStatus("REJECTED");
        request.setRejectionReason(rejectionReason);
        request.setApprover(approver);
        request.setApprovedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        FundingRequest savedRequest = fundingRequestRepository.save(request);
        LOGGER.info("Rejected funding request: ID=" + savedRequest.getId());
        return savedRequest;
    }

    public FundingRequest getFundingRequestById(UUID fundingRequestId) {
        FundingRequest request = fundingRequestRepository.findById(fundingRequestId)
                .orElseThrow(() -> new NotFoundException("Funding request not found with ID: " + fundingRequestId));
        LOGGER.info("Retrieved funding request: ID=" + request.getId());
        return request;
    }

    @Transactional
    public FundingRequest updateFundingRequest(UUID fundingRequestId, BigDecimal requestedAmount, String requestType,
                                               String reason, UUID budgetId, UUID budgetCategoryId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"FINANCE_OFFICER".equals(requester.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can update funding requests");
        }
        FundingRequest request = fundingRequestRepository.findById(fundingRequestId)
                .orElseThrow(() -> new NotFoundException("Funding request not found with ID: " + fundingRequestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING funding requests can be updated");
        }
        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new ForbiddenException("You can only update your own funding requests");
        }
        Park park = request.getPark();
        if (!park.getId().equals(requester.getPark().getId())) {
            throw new ForbiddenException("You can only update requests for your assigned park");
        }
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if (!"APPROVED".equals(budget.getStatus())) {
            throw new BadRequestException("Funding requests can only be made for APPROVED budgets");
        }
        if (!budget.getPark().getId().equals(park.getId())) {
            throw new BadRequestException("Budget does not belong to the specified park");
        }
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(budgetCategoryId)
                .orElseThrow(() -> new NotFoundException("Budget category not found with ID: " + budgetCategoryId));
        if (!budgetCategory.getBudget().getId().equals(budgetId)) {
            throw new BadRequestException("Budget category does not belong to the specified budget");
        }
        if (!List.of("EXTRA_FUNDS", "EMERGENCY_RELIEF").contains(requestType)) {
            throw new BadRequestException("Invalid request type: " + requestType);
        }
        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Requested amount must be positive");
        }

        request.setRequestedAmount(requestedAmount);
        request.setRequestType(requestType);
        request.setReason(reason);
        request.setBudget(budget);
        request.setBudgetCategory(budgetCategory);
        request.setUpdatedAt(LocalDateTime.now());
        FundingRequest savedRequest = fundingRequestRepository.save(request);
        LOGGER.info("Updated funding request: ID=" + savedRequest.getId());
        return savedRequest;
    }

    @Transactional
    public void deleteFundingRequest(UUID fundingRequestId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User requester = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"FINANCE_OFFICER".equals(requester.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER can delete funding requests");
        }
        FundingRequest request = fundingRequestRepository.findById(fundingRequestId)
                .orElseThrow(() -> new NotFoundException("Funding request not found with ID: " + fundingRequestId));
        if (!"PENDING".equals(request.getStatus())) {
            throw new BadRequestException("Only PENDING funding requests can be deleted");
        }
        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new ForbiddenException("You can only delete your own funding requests");
        }
        fundingRequestRepository.delete(request);
        LOGGER.info("Deleted funding request: ID=" + fundingRequestId);
    }

    public List<FundingRequest> getFundingRequestsByFiscalYear(int fiscalYear, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN, GOVERNMENT_OFFICER, or AUDITOR can view funding requests by fiscal year");
        }
        if (fiscalYear < 2000 || fiscalYear > LocalDate.now().getYear() + 1) {
            throw new BadRequestException("Invalid fiscal year: " + fiscalYear);
        }
        List<FundingRequest> requests = fundingRequestRepository.findByFiscalYear(fiscalYear);
        LOGGER.info("Retrieved " + requests.size() + " funding requests for fiscalYear: " + fiscalYear);
        return requests;
    }

    public List<FundingRequest> getFundingRequestsByPark(UUID parkId, Integer fiscalYear, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN, FINANCE_OFFICER, GOVERNMENT_OFFICER, or AUDITOR can view funding requests");
        }
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));
        if ("FINANCE_OFFICER".equals(user.getRole()) && !park.getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("FINANCE_OFFICER can only view funding requests for their assigned park");
        }
        List<FundingRequest> requests;
        int targetYear = (fiscalYear != null) ? fiscalYear : LocalDate.now().getYear();
        requests = fundingRequestRepository.findByParkIdAndFiscalYear(parkId, targetYear);
        LOGGER.info("Retrieved " + requests.size() + " funding requests for parkId: " + parkId + ", fiscalYear: " + targetYear);
        return requests;
    }

    public List<FundingRequest> getAllFundingRequests(Integer fiscalYear, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN, GOVERNMENT_OFFICER, or AUDITOR can view all funding requests");
        }
        List<FundingRequest> requests;
        if (fiscalYear != null) {
            requests = fundingRequestRepository.findAll().stream()
                    .filter(fr -> fr.getBudget().getFiscalYear() == fiscalYear)
                    .toList();
        } else {
            requests = fundingRequestRepository.findAll();
        }
        LOGGER.info("Retrieved " + requests.size() + " funding requests" + (fiscalYear != null ? " for fiscalYear: " + fiscalYear : ""));
        return requests;
    }

    public List<FundingRequest> getFundingRequestsByBudget(UUID budgetId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("ADMIN", "FINANCE_OFFICER", "GOVERNMENT_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only ADMIN, FINANCE_OFFICER, GOVERNMENT_OFFICER, or AUDITOR can view funding requests");
        }
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));
        if ("FINANCE_OFFICER".equals(user.getRole()) && !budget.getPark().getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("FINANCE_OFFICER can only view funding requests for their assigned park");
        }
        List<FundingRequest> requests = fundingRequestRepository.findByBudgetId(budgetId);
        LOGGER.info("Retrieved " + requests.size() + " funding requests for budgetId: " + budgetId);
        return requests;
    }
}