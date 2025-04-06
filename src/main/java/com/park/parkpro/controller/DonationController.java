// src/main/java/com/park/parkpro/controller/DonationController.java
package com.park.parkpro.controller;

import com.park.parkpro.domain.Donation;
import com.park.parkpro.dto.CreateDonationRequestDto;
import com.park.parkpro.dto.DonationResponseDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.DonationService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DonationController {
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
        Donation donation = donationService.createDonation(request.getParkId(), request.getAmount().toString(), paymentMethodId, request.getMotiveForDonation(), token);
        return ResponseEntity.created(URI.create("/api/donations/" + donation.getId()))
                .body(mapToDonationDto(donation));
    }

    @PostMapping("/donations/{donationId}/confirm")
    public ResponseEntity<DonationResponseDto> confirmDonation(
            @PathVariable UUID donationId,
            @RequestHeader("Authorization") String authHeader) throws StripeException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Donation donation = donationService.confirmDonation(donationId, token);
        return ResponseEntity.ok(mapToDonationDto(donation));
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
        Donation donation = donationService.getDonationById(donationId);
        return ResponseEntity.ok(mapToDonationDto(donation));
    }

    private DonationResponseDto mapToDonationDto(Donation donation) {
        return new DonationResponseDto(
                donation.getId(), donation.getDonor().getId(), donation.getPark().getId(),
                donation.getAmount(), donation.getStatus(), donation.getPaymentReference(),
                donation.getCurrency(), donation.getMotiveForDonation(), // Include new field
                donation.getConfirmedAt(), donation.getCreatedAt(), donation.getUpdatedAt()
        );
    }
}