package com.park.parkpro.service;

import com.park.parkpro.domain.AuditLog;
import com.park.parkpro.domain.User;
import com.park.parkpro.dto.AuditLogDto;
import com.park.parkpro.dto.CreateAuditLogDto;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.AuditLogRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuditLogDto createAuditLog(CreateAuditLogDto dto, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        AuditLog auditLog = new AuditLog(
                dto.getAction(),
                dto.getEntityType(),
                dto.getEntityId(),
                dto.getDetails(),
                user,
                LocalDateTime.now()
        );

        auditLog = auditLogRepository.save(auditLog);
        return mapToDto(auditLog);
    }

    public List<AuditLogDto> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AuditLogDto> getAllAuditLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AuditLogDto mapToDto(AuditLog auditLog) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(auditLog.getId());
        dto.setAction(auditLog.getAction());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setDetails(auditLog.getDetails());
        dto.setPerformedById(auditLog.getPerformedBy() != null ? auditLog.getPerformedBy().getId() : null);
        dto.setPerformedAt(auditLog.getPerformedAt());
        dto.setReviewedById(auditLog.getReviewedBy() != null ? auditLog.getReviewedBy().getId() : null);
        dto.setReviewedAt(auditLog.getReviewedAt());
        return dto;
    }
}