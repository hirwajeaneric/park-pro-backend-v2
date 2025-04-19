package com.park.parkpro.controller;

import com.park.parkpro.dto.AuditLogDto;
import com.park.parkpro.dto.CreateAuditLogDto;
import com.park.parkpro.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<AuditLogDto> createAuditLog(@RequestBody CreateAuditLogDto dto,
                                                      @RequestHeader("Authorization") String token) {
        AuditLogDto createdLog = auditLogService.createAuditLog(dto, token.replace("Bearer ", ""));
        return ResponseEntity.ok(createdLog);
    }

    @GetMapping
    public ResponseEntity<List<AuditLogDto>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLogDto>> getAuditLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntityType(entityType));
    }
}