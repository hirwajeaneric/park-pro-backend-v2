package com.park.parkpro.service;

import com.park.parkpro.domain.Donation;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.DonationRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class DonationService {
    private static final Logger LOGGER = Logger.getLogger(DonationService.class.getName());
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final ParkRepository parkRepository;
    private final BudgetRepository budgetRepository;
    private final JwtUtil jwtUtil;

    public DonationService(DonationRepository donationRepository, UserRepository userRepository,
                           ParkRepository parkRepository, BudgetRepository budgetRepository, JwtUtil jwtUtil) {
        this.donationRepository = donationRepository;
        this.userRepository = userRepository;
        this.parkRepository = parkRepository;
        this.budgetRepository = budgetRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Donation createDonation(UUID parkId, String amount, String motiveForDonation, String paymentMethodId, String token) throws StripeException {
        String email = jwtUtil.getEmailFromToken(token);
        User donor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new NotFoundException("Park not found with ID: " + parkId));

        BigDecimal donationAmount;
        try {
            donationAmount = new BigDecimal(amount);
            if (donationAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }

        // Determine fiscal year
        int currentFiscalYear = LocalDate.now().getYear();
        int fiscalYear = currentFiscalYear;

        // Payment processing with Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(donationAmount.multiply(new BigDecimal("100")).longValue())
                .setCurrency("XAF")
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setDescription("Donation to park: " + park.getName())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Donation donation = new Donation();
        donation.setDonor(donor);
        donation.setPark(park);
        donation.setAmount(donationAmount);
        donation.setStatus("CONFIRMED");
        donation.setPaymentReference(paymentIntent.getId());
        donation.setCurrency("XAF");
        donation.setMotiveForDonation(motiveForDonation);
        donation.setFiscalYear(fiscalYear);
        donation.setConfirmedAt(LocalDateTime.now());

        Donation savedDonation = donationRepository.save(donation);
        LOGGER.info("Saved donation: ID=" + savedDonation.getId() + ", Amount=" + savedDonation.getAmount());
        return savedDonation;
    }

    @Transactional
    public Donation cancelDonation(UUID donationId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new NotFoundException("Donation not found with ID: " + donationId));

        if (!"CONFIRMED".equals(donation.getStatus())) {
            throw new IllegalStateException("Only CONFIRMED donations can be cancelled");
        }

        donation.setStatus("CANCELLED");
        donation.setUpdatedAt(LocalDateTime.now());

        // Refund via Stripe (simplified)
        // In practice, implement Stripe refund logic here

        // Revert budget balance
        budgetRepository.findByParkIdAndFiscalYear(donation.getPark().getId(), donation.getFiscalYear())
                .ifPresent(budget -> {
                    budget.setBalance(budget.getBalance().subtract(donation.getAmount()));
                    budget.setUpdatedAt(LocalDateTime.now());
                    budgetRepository.save(budget);
                });

        return donationRepository.save(donation);
    }

    public List<Donation> getDonationsByDonor(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User donor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return donationRepository.findByDonorId(donor.getId());
    }

    public List<Donation> getDonationsByPark(UUID parkId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return donationRepository.findByParkId(parkId);
    }

    public Donation getDonationById(UUID donationId) {
        LOGGER.info("Querying donation ID: " + donationId);
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new NotFoundException("Donation not found with ID: " + donationId));
        LOGGER.info("Found donation: ID=" + donation.getId() + ", Amount=" + donation.getAmount());
        return donation;
    }
}