package com.park.parkpro.controller;

import com.park.parkpro.dto.AuditResponseDto;
import com.park.parkpro.dto.CreateAuditRequestDto;
import com.park.parkpro.dto.UpdateAuditRequestDto;
import com.park.parkpro.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<AuditResponseDto> createAudit(
            @Valid @RequestBody CreateAuditRequestDto request,
            @RequestHeader("Authorization") String token) {
        token = token.substring(7); // Remove "Bearer " prefix
        return new ResponseEntity<>(auditService.createAudit(request, token), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AuditResponseDto>> getAllAudits() {
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<AuditResponseDto>> getAuditsByYear(@PathVariable Integer year) {
        return ResponseEntity.ok(auditService.getAuditsByYear(year));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditResponseDto> getAuditById(@PathVariable UUID id) {
        return ResponseEntity.ok(auditService.getAuditById(id));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<AuditResponseDto> updateAuditProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAuditRequestDto request,
            @RequestHeader("Authorization") String token) {
        token = token.substring(7); // Remove "Bearer " prefix
        return ResponseEntity.ok(auditService.updateAuditProgress(id, request, token));
    }
}

