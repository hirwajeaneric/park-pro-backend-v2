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
    private final ExpenseRepository expenseRepository;
    private final BookingRepository bookingRepository;
    private final DonationRepository donationRepository;
    private final FundingRequestRepository fundingRequestRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final JwtUtil jwtUtil;

    public BudgetService(BudgetRepository budgetRepository, ParkRepository parkRepository,
                         UserRepository userRepository, IncomeStreamRepository incomeStreamRepository,
                         ExpenseRepository expenseRepository, BookingRepository bookingRepository,
                         DonationRepository donationRepository, FundingRequestRepository fundingRequestRepository,
                         WithdrawRequestRepository withdrawRequestRepository, JwtUtil jwtUtil) {
        this.budgetRepository = budgetRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.incomeStreamRepository = incomeStreamRepository;
        this.expenseRepository = expenseRepository;
        this.bookingRepository = bookingRepository;
        this.donationRepository = donationRepository;
        this.fundingRequestRepository = fundingRequestRepository;
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.jwtUtil = jwtUtil;
    }

    private BigDecimal calculateBudgetBalance(Budget budget) {
        UUID budgetId = budget.getId();
        Integer fiscalYear = budget.getFiscalYear();
        UUID parkId = budget.getPark().getId();

        // Get base balance from budget
        BigDecimal balance = budget.getBalance();

        // Subtract expenses
        BigDecimal totalExpenses = expenseRepository.findByBudgetId(budgetId).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance = balance.subtract(totalExpenses);

        // Subtract approved withdraw requests
        BigDecimal totalWithdraws = withdrawRequestRepository.findByBudgetId(budgetId).stream()
                .filter(request -> WithdrawRequest.WithdrawRequestStatus.APPROVED.equals(request.getStatus()))
                .map(WithdrawRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance = balance.subtract(totalWithdraws);

        // Add confirmed bookings for the fiscal year
        BigDecimal totalBookings = bookingRepository.findByParkId(parkId).stream()
                .filter(booking -> {
                    int bookingFiscalYear = booking.getVisitDate().getYear();
                    return bookingFiscalYear == fiscalYear && "CONFIRMED".equals(booking.getStatus());
                })
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance = balance.add(totalBookings);

        // Add confirmed donations
        BigDecimal totalDonations = donationRepository.findByParkIdAndFiscalYear(parkId, fiscalYear).stream()
                .filter(donation -> "CONFIRMED".equals(donation.getStatus()))
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance = balance.add(totalDonations);

        // Add approved funding requests
        BigDecimal totalApprovedFunding = fundingRequestRepository.findByBudgetId(budgetId).stream()
                .filter(request -> "APPROVED".equals(request.getStatus()))
                .map(FundingRequest::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        balance = balance.add(totalApprovedFunding);

        return balance;
    }

    public Budget getBudgetById(UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with id: " + budgetId));
        budget.setBalance(calculateBudgetBalance(budget));
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

        // Find government income stream and update budget balance
        List<IncomeStream> governmentStreams = incomeStreamRepository.findByBudgetIdAndNameContaining(budgetId, "Government");
        System.out.println("Government streams: " + governmentStreams);
        if (governmentStreams.isEmpty()) {
            throw new BadRequestException("No government income stream found for this budget");
        }

        budget.setBalance(governmentStreams.get(0).getTotalContribution());
        budget.setStatus("APPROVED");
        budget.setApprovedBy(approver);
        budget.setApprovedAt(LocalDateTime.now());
        
        // Top up government income streams actual balance
        governmentStreams.forEach(is -> {
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
                .peek(budget -> budget.setBalance(calculateBudgetBalance(budget)))
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
            
            BigDecimal balance = budget != null ? calculateBudgetBalance(budget) : null;
            
            return new BudgetByFiscalYearResponseDto(
                    budget != null ? budget.getId() : null,
                    park.getId(),
                    park.getName(),
                    fiscalYear,
                    budget != null ? budget.getTotalAmount() : null,
                    balance,
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