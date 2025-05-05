package com.park.parkpro.controller;

import com.park.parkpro.domain.FundingRequest;
import com.park.parkpro.dto.CreateFundingRequestDto;
import com.park.parkpro.dto.FundingRequestResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.FundingRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FundingRequestController {
    private static final Logger LOGGER = Logger.getLogger(FundingRequestController.class.getName());
    private final FundingRequestService fundingRequestService;

    public FundingRequestController(FundingRequestService fundingRequestService) {
        this.fundingRequestService = fundingRequestService;
    }

    @PostMapping("/parks/{parkId}/funding-requests")
    public ResponseEntity<FundingRequestResponseDto> createFundingRequest(
            @PathVariable UUID parkId,
            @Valid @RequestBody CreateFundingRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Creating funding request for parkId: " + parkId);
        FundingRequest fundingRequest = fundingRequestService.createFundingRequest(
                parkId, request.getRequestedAmount(), request.getRequestType(),
                request.getReason(), request.getBudgetId(), token);
        return ResponseEntity.created(URI.create("/api/funding-requests/" + fundingRequest.getId()))
                .body(mapToFundingRequestDto(fundingRequest));
    }

    @PostMapping("/funding-requests/{fundingRequestId}/approve")
    public ResponseEntity<FundingRequestResponseDto> approveFundingRequest(
            @PathVariable UUID fundingRequestId,
            @RequestParam BigDecimal approvedAmount,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Approving funding request: " + fundingRequestId);
        FundingRequest fundingRequest = fundingRequestService.approveFundingRequest(fundingRequestId, approvedAmount, token);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @PostMapping("/funding-requests/{fundingRequestId}/reject")
    public ResponseEntity<FundingRequestResponseDto> rejectFundingRequest(
            @PathVariable UUID fundingRequestId,
            @RequestParam String rejectionReason,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Rejecting funding request: " + fundingRequestId);
        FundingRequest fundingRequest = fundingRequestService.rejectFundingRequest(fundingRequestId, rejectionReason, token);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @GetMapping("/funding-requests/{fundingRequestId}")
    public ResponseEntity<FundingRequestResponseDto> getFundingRequestById(
            @PathVariable UUID fundingRequestId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        LOGGER.info("Fetching funding request: " + fundingRequestId);
        FundingRequest fundingRequest = fundingRequestService.getFundingRequestById(fundingRequestId);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @PatchMapping("/funding-requests/{fundingRequestId}")
    public ResponseEntity<FundingRequestResponseDto> updateFundingRequest(
            @PathVariable UUID fundingRequestId,
            @Valid @RequestBody CreateFundingRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Updating funding request: " + fundingRequestId);
        FundingRequest fundingRequest = fundingRequestService.updateFundingRequest(
                fundingRequestId, request.getRequestedAmount(), request.getRequestType(),
                request.getReason(), request.getBudgetId(), token);
        return ResponseEntity.ok(mapToFundingRequestDto(fundingRequest));
    }

    @DeleteMapping("/funding-requests/{fundingRequestId}")
    public ResponseEntity<Void> deleteFundingRequest(
            @PathVariable UUID fundingRequestId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Deleting funding request: " + fundingRequestId);
        fundingRequestService.deleteFundingRequest(fundingRequestId, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parks/{parkId}/funding-requests")
    public ResponseEntity<List<FundingRequestResponseDto>> getFundingRequestsByPark(
            @PathVariable UUID parkId,
            @RequestParam(required = false) Integer fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching funding requests for parkId: " + parkId + ", fiscalYear: " + (fiscalYear != null ? fiscalYear : "current"));
        List<FundingRequest> fundingRequests = fundingRequestService.getFundingRequestsByPark(parkId, fiscalYear, token);
        return ResponseEntity.ok(fundingRequests.stream()
                .map(this::mapToFundingRequestDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/budgets/{budgetId}/funding-requests")
    public ResponseEntity<List<FundingRequestResponseDto>> getFundingRequestsByBudget(
            @PathVariable UUID budgetId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching funding requests for budgetId: " + budgetId);
        List<FundingRequest> fundingRequests = fundingRequestService.getFundingRequestsByBudget(budgetId, token);
        return ResponseEntity.ok(fundingRequests.stream()
                .map(this::mapToFundingRequestDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/funding-requests")
    public ResponseEntity<List<FundingRequestResponseDto>> getAllFundingRequests(
            @RequestParam(required = false) Integer fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching all funding requests" + (fiscalYear != null ? " for fiscalYear: " + fiscalYear : ""));
        List<FundingRequest> fundingRequests = fundingRequestService.getAllFundingRequests(fiscalYear, token);
        return ResponseEntity.ok(fundingRequests.stream()
                .map(this::mapToFundingRequestDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/funding-requests/fiscal-year/{fiscalYear}")
    public ResponseEntity<List<FundingRequestResponseDto>> getFundingRequestsByFiscalYear(
            @PathVariable int fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching funding requests for fiscalYear: " + fiscalYear);
        List<FundingRequest> fundingRequests = fundingRequestService.getFundingRequestsByFiscalYear(fiscalYear, token);
        return ResponseEntity.ok(fundingRequests.stream()
                .map(this::mapToFundingRequestDto)
                .collect(Collectors.toList()));
    }

    private FundingRequestResponseDto mapToFundingRequestDto(FundingRequest fundingRequest) {
        return new FundingRequestResponseDto(
                fundingRequest.getId(),
                fundingRequest.getPark().getId(),
                fundingRequest.getPark().getName(),
                fundingRequest.getBudget().getId(),
                fundingRequest.getRequestedAmount(),
                fundingRequest.getApprovedAmount(),
                fundingRequest.getRequestType(),
                fundingRequest.getReason(),
                fundingRequest.getRequester().getId(),
                fundingRequest.getApprover() != null ? fundingRequest.getApprover().getId() : null,
                fundingRequest.getStatus(),
                fundingRequest.getRejectionReason(),
                fundingRequest.getApprovedAt(),
                fundingRequest.getCurrency(),
                fundingRequest.getCreatedAt(),
                fundingRequest.getUpdatedAt()
        );
    }
}