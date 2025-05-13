package com.park.parkpro.controller;

import com.park.parkpro.domain.Donation;
import com.park.parkpro.dto.CreateDonationRequestDto;
import com.park.parkpro.dto.DonationResponseDto;
import com.park.parkpro.dto.OutstandingDonorResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.DonationService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class DonationController {
    private static final Logger LOGGER = Logger.getLogger(DonationController.class.getName());
    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping("/donations")
    public ResponseEntity<DonationResponseDto> createDonation(
            @Valid @RequestBody CreateDonationRequestDto request,
            @RequestParam String paymentMethodId,
            @RequestHeader("Authorization") String authHeader) throws StripeException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Donation donation = donationService.createDonation(
                request.getParkId(),
                request.getAmount().toString(),
                request.getMotiveForDonation(),
                paymentMethodId,
                token
        );
        return ResponseEntity.created(URI.create("/api/donations/" + donation.getId()))
                .body(mapToDonationDto(donation));
    }

    @PostMapping("/donations/{donationId}/cancel")
    public ResponseEntity<DonationResponseDto> cancelDonation(
            @PathVariable UUID donationId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Donation donation = donationService.cancelDonation(donationId, token);
        return ResponseEntity.ok(mapToDonationDto(donation));
    }

    @GetMapping("/donations/my")
    public ResponseEntity<List<DonationResponseDto>> getMyDonations(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Donation> donations = donationService.getDonationsByDonor(token);
        return ResponseEntity.ok(donations.stream().map(this::mapToDonationDto).collect(Collectors.toList()));
    }

    @GetMapping("/parks/{parkId}/donations/fiscal-year/{fiscalYear}")
    public ResponseEntity<List<DonationResponseDto>> getDonationsByParkAndFiscalYear(
            @PathVariable UUID parkId,
            @PathVariable int fiscalYear,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching donations for parkId: " + parkId + ", fiscalYear: " + fiscalYear);
        List<Donation> donations = donationService.getDonationsByParkAndFiscalYear(parkId, fiscalYear, token);
        return ResponseEntity.ok(donations.stream().map(this::mapToDonationDto).collect(Collectors.toList()));
    }

    @GetMapping("/parks/{parkId}/top-donors")
    public ResponseEntity<List<OutstandingDonorResponseDto>> getTopDonorsByPark(@PathVariable UUID parkId) {
        LOGGER.info("Fetching top donors for parkId: " + parkId);
        List<OutstandingDonorResponseDto> donors = donationService.getTopDonorsByPark(parkId);
        return ResponseEntity.ok(donors);
    }

    @GetMapping("/parks/{parkId}/donations")
    public ResponseEntity<List<DonationResponseDto>> getDonationsByPark(
            @PathVariable UUID parkId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        List<Donation> donations = donationService.getDonationsByPark(parkId, token);
        return ResponseEntity.ok(donations.stream().map(this::mapToDonationDto).collect(Collectors.toList()));
    }

    @GetMapping("/donations/{donationId}")
    public ResponseEntity<DonationResponseDto> getDonationById(@PathVariable UUID donationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LOGGER.info("Fetching donation ID: " + donationId + ", User: " +
                (authentication != null ? authentication.getName() : "anonymous") +
                ", Roles: " + (authentication != null ? authentication.getAuthorities() : "none"));
        Donation donation = donationService.getDonationById(donationId);
        LOGGER.info("Found donation: " + donation.getId() + ", Amount: " + donation.getAmount());
        return ResponseEntity.ok(mapToDonationDto(donation));
    }

    private DonationResponseDto mapToDonationDto(Donation donation) {
        return new DonationResponseDto(
                donation.getId(),
                donation.getDonor().getId(),
                donation.getDonor().getFirstName()+" "+donation.getDonor().getLastName(),
                donation.getPark().getId(),
                donation.getAmount(),
                donation.getStatus(),
                donation.getPaymentReference(),
                donation.getCurrency(),
                donation.getMotiveForDonation(),
                donation.getFiscalYear(),
                donation.getConfirmedAt(),
                donation.getCreatedAt(),
                donation.getUpdatedAt()
        );
    }
}