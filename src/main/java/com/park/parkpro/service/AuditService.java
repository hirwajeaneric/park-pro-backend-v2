package com.park.parkpro.service;

import com.park.parkpro.domain.*;
import com.park.parkpro.dto.AuditResponseDto;
import com.park.parkpro.dto.CreateAuditRequestDto;
import com.park.parkpro.dto.UpdateAuditRequestDto;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.*;
import com.park.parkpro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository auditRepository;
    private final ParkRepository parkRepository;
    private final ExpenseRepository expenseRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuditResponseDto createAudit(CreateAuditRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Park park = parkRepository.findById(request.getParkId())
                .orElseThrow(() -> new NotFoundException("Park not found"));

        // Check if audit already exists for the year
        auditRepository.findByParkIdAndAuditYear(request.getParkId(), request.getAuditYear())
                .ifPresent(audit -> {
                    throw new IllegalStateException("Audit already exists for this park and year");
                });

        // Calculate audit statistics
        List<Expense> expenses = expenseRepository.findByParkIdAndYear(request.getParkId(), request.getAuditYear());
        List<WithdrawRequest> withdrawRequests = withdrawRequestRepository.findByParkIdAndYear(request.getParkId(), request.getAuditYear());

        int totalItems = expenses.size() + withdrawRequests.size();
        if (totalItems == 0) {
            throw new IllegalStateException("No expenses or withdraw requests found for the specified year");
        }

        long passedCount = expenses.stream().filter(e -> e.getAuditStatus() == AuditStatus.PASSED).count() +
                withdrawRequests.stream().filter(w -> w.getAuditStatus() == AuditStatus.PASSED).count();

        long failedCount = expenses.stream().filter(e -> e.getAuditStatus() == AuditStatus.FAILED).count() +
                withdrawRequests.stream().filter(w -> w.getAuditStatus() == AuditStatus.FAILED).count();

        long unjustifiedCount = expenses.stream().filter(e -> e.getAuditStatus() == AuditStatus.UNJUSTIFIED).count() +
                withdrawRequests.stream().filter(w -> w.getAuditStatus() == AuditStatus.UNJUSTIFIED).count();

        double percentagePassed = (double) passedCount / totalItems * 100;
        double percentageFailed = (double) failedCount / totalItems * 100;
        double percentageUnjustified = (double) unjustifiedCount / totalItems * 100;

        Audit audit = Audit.builder()
                .park(park)
                .auditYear(request.getAuditYear())
                .percentagePassed(percentagePassed)
                .percentageFailed(percentageFailed)
                .percentageUnjustified(percentageUnjustified)
                .totalPercentage(percentagePassed)
                .auditProgress(AuditProgress.IN_PROGRESS)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        audit = auditRepository.save(audit);
        return mapToDto(audit);
    }

    public List<AuditResponseDto> getAllAudits() {
        return auditRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AuditResponseDto> getAuditsByYear(Integer year) {
        return auditRepository.findByAuditYear(year).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AuditResponseDto getAuditById(UUID id) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Audit not found"));
        return mapToDto(audit);
    }

    @Transactional
    public AuditResponseDto updateAuditProgress(UUID id, UpdateAuditRequestDto request, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Audit not found"));

        audit.setAuditProgress(request.getAuditProgress());
        audit.setUpdatedBy(currentUser);
        audit.setUpdatedAt(LocalDateTime.now());
        
        audit = auditRepository.save(audit);
        return mapToDto(audit);
    }

    private AuditResponseDto mapToDto(Audit audit) {
        return AuditResponseDto.builder()
                .id(audit.getId())
                .parkId(audit.getPark().getId())
                .parkName(audit.getPark().getName())
                .auditYear(audit.getAuditYear())
                .percentagePassed(audit.getPercentagePassed())
                .percentageFailed(audit.getPercentageFailed())
                .percentageUnjustified(audit.getPercentageUnjustified())
                .totalPercentage(audit.getTotalPercentage())
                .auditProgress(audit.getAuditProgress())
                .createdBy(audit.getCreatedBy().getEmail())
                .updatedBy(audit.getUpdatedBy().getEmail())
                .createdAt(audit.getCreatedAt())
                .updatedAt(audit.getUpdatedAt())
                .build();
    }
}

