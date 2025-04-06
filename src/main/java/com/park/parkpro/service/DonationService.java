// src/main/java/com/park/parkpro/service/DonationService.java
package com.park.parkpro.service;

import com.park.parkpro.domain.Donation;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.DonationRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final StripeService stripeService;

    public DonationService(DonationRepository donationRepository, ParkRepository parkRepository,
                           UserRepository userRepository, JwtUtil jwtUtil, StripeService stripeService) {
        this.donationRepository = donationRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.stripeService = stripeService;
    }

    @Transactional
    public Donation createDonation(UUID parkId, String amount, String paymentMethodId, String motiveForDonation, String token) throws StripeException {
        String email = jwtUtil.getEmailFromToken(token);
        User donor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"VISITOR".equals(donor.getRole())) {
            throw new ForbiddenException("Only VISITOR can make donations");
        }
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));

        // Create PaymentIntent
        Long amountInCents = new java.math.BigDecimal(amount).multiply(new java.math.BigDecimal("100")).longValue();
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                amountInCents, "XAF", "Donation to " + park.getName(), "temp_donation_id");

        // Attach payment method
        paymentIntent = paymentIntent.update(Map.of("payment_method", paymentMethodId));

        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setPark(park);
        donation.setAmount(new java.math.BigDecimal(amount));
        donation.setStatus("PENDING");
        donation.setPaymentReference(paymentIntent.getId());
        donation.setMotiveForDonation(motiveForDonation); // Set the new field
        donation = donationRepository.save(donation);

        // Update metadata with actual donation ID
        paymentIntent.update(Map.of("metadata", Map.of("donation_id", donation.getId().toString())));

        return donation;
    }

    @Transactional
    public Donation confirmDonation(UUID donationId, String token) throws StripeException {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can confirm donations");
        }
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new NotFoundException("Donation not found with ID: " + donationId));
        if (!"PENDING".equals(donation.getStatus())) {
            throw new BadRequestException("Only PENDING donations can be confirmed");
        }
        if ("PARK_MANAGER".equals(user.getRole()) && !donation.getPark().getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only confirm donations for their assigned park");
        }

        // Confirm PaymentIntent
        PaymentIntent paymentIntent = stripeService.confirmPaymentIntent(donation.getPaymentReference());
        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new BadRequestException("Payment failed: " + paymentIntent.getLastPaymentError().getMessage());
        }

        donation.setStatus("CONFIRMED");
        donation.setConfirmedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        return donationRepository.save(donation); // Trigger updates budget
    }

    @Transactional
    public Donation cancelDonation(UUID donationId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new NotFoundException("Donation not found with ID: " + donationId));

        if ("VISITOR".equals(user.getRole()) && !donation.getDonor().getId().equals(user.getId())) {
            throw new ForbiddenException("VISITOR can only cancel their own donations");
        }
        if (List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            if ("PARK_MANAGER".equals(user.getRole()) && !donation.getPark().getId().equals(user.getPark().getId())) {
                throw new ForbiddenException("PARK_MANAGER can only cancel donations for their assigned park");
            }
        } else if (!"VISITOR".equals(user.getRole())) {
            throw new ForbiddenException("Only VISITOR, PARK_MANAGER, or ADMIN can cancel donations");
        }
        if ("CANCELLED".equals(donation.getStatus())) {
            throw new BadRequestException("Donation is already cancelled");
        }

        donation.setStatus("CANCELLED");
        donation.setUpdatedAt(LocalDateTime.now());
        return donationRepository.save(donation);
    }

    public List<Donation> getDonationsByDonor(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User donor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"VISITOR".equals(donor.getRole())) {
            throw new ForbiddenException("Only VISITOR can view their own donations");
        }
        return donationRepository.findByDonorId(donor.getId());
    }

    public List<Donation> getDonationsByPark(UUID parkId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER, ADMIN, or FINANCE_OFFICER can view park donations");
        }
        if ("PARK_MANAGER".equals(user.getRole()) && !parkId.equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only view donations for their assigned park");
        }
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park not found with ID: " + parkId);
        }
        return donationRepository.findByParkId(parkId);
    }

    public Donation getDonationById(UUID donationId) {
        return donationRepository.findById(donationId)
                .orElseThrow(() -> new NotFoundException("Donation not found with ID: " + donationId));
    }
}