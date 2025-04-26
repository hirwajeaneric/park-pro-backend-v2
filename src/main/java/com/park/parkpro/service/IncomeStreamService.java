package com.park.parkpro.service;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.IncomeStreamRequestDto;
import com.park.parkpro.dto.IncomeStreamResponseDto;
import com.park.parkpro.exception.*;
import com.park.parkpro.repository.*;
import com.park.parkpro.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IncomeStreamService {
    private final IncomeStreamRepository incomeStreamRepository;
    private final BudgetRepository budgetRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public IncomeStreamService(IncomeStreamRepository incomeStreamRepository, BudgetRepository budgetRepository,
                               ParkRepository parkRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.incomeStreamRepository = incomeStreamRepository;
        this.budgetRepository = budgetRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public IncomeStreamResponseDto createIncomeStream(UUID budgetId, IncomeStreamRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(createdBy.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can create income streams");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found with ID: " + budgetId));

        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be added to DRAFT budgets");
        }

        if (incomeStreamRepository.existsByBudgetIdAndName(budgetId, request.getName())) {
            throw new ConflictException("Income stream with name '" + request.getName() + "' already exists for this budget");
        }

        // Additional validation (complements database trigger)
        BigDecimal sumPercentage = incomeStreamRepository.findByBudgetId(budgetId).stream()
                .map(IncomeStream::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumPercentage.add(request.getPercentage()).compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Total percentage exceeds 100%");
        }

        BigDecimal sumContribution = incomeStreamRepository.findByBudgetId(budgetId).stream()
                .map(IncomeStream::getTotalContribution)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumContribution.add(request.getTotalContribution()).compareTo(budget.getTotalAmount()) > 0) {
            throw new BadRequestException("Total contribution exceeds budget total amount");
        }

        IncomeStream incomeStream = new IncomeStream();
        incomeStream.setBudget(budget);
        incomeStream.setPark(budget.getPark());
        incomeStream.setFiscalYear(budget.getFiscalYear());
        incomeStream.setName(request.getName());
        incomeStream.setPercentage(request.getPercentage());
        incomeStream.setTotalContribution(request.getTotalContribution());
        incomeStream.setCreatedBy(createdBy);

        incomeStream = incomeStreamRepository.save(incomeStream);
        return mapToDto(incomeStream);
    }

    @Transactional
    public IncomeStreamResponseDto updateIncomeStream(UUID incomeStreamId, IncomeStreamRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can update income streams");
        }

        IncomeStream incomeStream = incomeStreamRepository.findById(incomeStreamId)
                .orElseThrow(() -> new NotFoundException("Income stream not found with ID: " + incomeStreamId));

        Budget budget = incomeStream.getBudget();
        if (!"DRAFT".equals(budget.getStatus())) {
            throw new BadRequestException("Income streams can only be updated for DRAFT budgets");
        }

        if (!request.getName().equals(incomeStream.getName()) &&
                incomeStreamRepository.existsByBudgetIdAndName(budget.getId(), request.getName())) {
            throw new ConflictException("Income stream with name '" + request.getName() + "' already exists for this budget");
        }

        // Validation for percentage and contribution
        BigDecimal sumPercentage = incomeStreamRepository.findByBudgetId(budget.getId()).stream()
                .filter(is -> !is.getId().equals(incomeStreamId))
                .map(IncomeStream::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumPercentage.add(request.getPercentage()).compareTo(new BigDecimal("100")) > 0) {
            throw new BadRequestException("Total percentage exceeds 100%");
        }

        BigDecimal sumContribution = incomeStreamRepository.findByBudgetId(budget.getId()).stream()
                .filter(is -> !is.getId().equals(incomeStreamId))
                .map(IncomeStream::getTotalContribution)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumContribution.add(request.getTotalContribution()).compareTo(budget.getTotalAmount()) > 0) {
            throw new BadRequestException("Total contribution exceeds budget total amount");
        }

        incomeStream.setName(request.getName());
        incomeStream.setPercentage(request.getPercentage());
        incomeStream.setTotalContribution(request.getTotalContribution());

        incomeStream = incomeStreamRepository.save(incomeStream);
        return mapToDto(incomeStream);
    }

    @Transactional
    public void deleteIncomeStream(UUID incomeStreamId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        if (!List.of("FINANCE_OFFICER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only FINANCE_OFFICER or ADMIN can delete income streams");
        }

        IncomeStream incomeStream = incomeStreamRepository.findById(incomeStreamId)
                .orElseThrow(() -> new NotFoundException("Income stream not found with ID: " + incomeStreamId));

        if (!"DRAFT".equals(incomeStream.getBudget().getStatus())) {
            throw new BadRequestException("Income streams can only be deleted for DRAFT budgets");
        }

        incomeStreamRepository.delete(incomeStream);
    }

    public IncomeStreamResponseDto getIncomeStreamById(UUID incomeStreamId) {
        IncomeStream incomeStream = incomeStreamRepository.findById(incomeStreamId)
                .orElseThrow(() -> new NotFoundException("Income stream not found with ID: " + incomeStreamId));
        return mapToDto(incomeStream);
    }

    public List<IncomeStreamResponseDto> getIncomeStreamsByBudget(UUID budgetId) {
        List<IncomeStream> incomeStreams = incomeStreamRepository.findByBudgetId(budgetId);
        return incomeStreams.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private IncomeStreamResponseDto mapToDto(IncomeStream incomeStream) {
        return new IncomeStreamResponseDto(
                incomeStream.getId(),
                incomeStream.getBudget().getId(),
                incomeStream.getPark().getId(),
                incomeStream.getFiscalYear(),
                incomeStream.getName(),
                incomeStream.getPercentage(),
                incomeStream.getTotalContribution(),
                incomeStream.getCreatedBy().getId(),
                incomeStream.getCreatedAt(),
                incomeStream.getUpdatedAt()
        );
    }
}