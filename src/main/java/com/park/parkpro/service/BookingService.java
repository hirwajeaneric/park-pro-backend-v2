package com.park.parkpro.service;

import com.park.parkpro.domain.Activity;
import com.park.parkpro.domain.Booking;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.ActivityRepository;
import com.park.parkpro.repository.BookingRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ActivityRepository activityRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final StripeService stripeService;

    public BookingService(BookingRepository bookingRepository, ActivityRepository activityRepository,
                          ParkRepository parkRepository, UserRepository userRepository, JwtUtil jwtUtil, StripeService stripeService) {
        this.bookingRepository = bookingRepository;
        this.activityRepository = activityRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.stripeService = stripeService;
    }

    @Transactional
    public Booking createBooking(UUID activityId, LocalDate visitDate, String paymentMethodId, String token) throws StripeException {
        String email = jwtUtil.getEmailFromToken(token);
        User visitor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"VISITOR".equals(visitor.getRole())) {
            throw new ForbiddenException("Only VISITOR can create bookings");
        }
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + activityId));
        Park park = activity.getPark();

        // Availability Check
        if (activity.getCapacityPerDay() != null) {
            long confirmedBookings = bookingRepository.countByActivityIdAndVisitDateAndStatus(
                    activityId, visitDate, "CONFIRMED");
            if (confirmedBookings >= activity.getCapacityPerDay()) {
                throw new BadRequestException("No available slots for this activity on " + visitDate);
            }
        }
        if (visitDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Visit date must be today or in the future");
        }

        // Create PaymentIntent
        Long amountInCents = activity.getPrice().multiply(new java.math.BigDecimal("100")).longValue();
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                amountInCents, "XAF", "Booking for " + activity.getName(), "temp_booking_id");

        // Attach payment method and set to requires_confirmation
        paymentIntent = paymentIntent.update(Map.of("payment_method", paymentMethodId));

        Booking booking = new Booking();
        booking.setVisitor(visitor);
        booking.setActivity(activity);
        booking.setAmount(activity.getPrice());
        booking.setPark(park);
        booking.setVisitDate(visitDate);
        booking.setStatus("PENDING");
        booking.setPaymentReference(paymentIntent.getId()); // Store PaymentIntent ID
        booking = bookingRepository.save(booking);

        // Update metadata with actual booking ID
        paymentIntent.update(Map.of("metadata", Map.of("booking_id", booking.getId().toString())));

        return booking;
    }

    @Transactional
    public Booking confirmBooking(UUID bookingId, String token) throws StripeException {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can confirm bookings");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));
        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Only PENDING bookings can be confirmed");
        }
        if ("PARK_MANAGER".equals(user.getRole()) && !booking.getPark().getId().equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only confirm bookings for their assigned park");
        }

        // Confirm PaymentIntent
        PaymentIntent paymentIntent = stripeService.confirmPaymentIntent(booking.getPaymentReference());
        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new BadRequestException("Payment failed: " + paymentIntent.getLastPaymentError().getMessage());
        }

        booking.setStatus("CONFIRMED");
        booking.setConfirmedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking); // Trigger updates budget
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));

        if ("VISITOR".equals(user.getRole()) && !booking.getVisitor().getId().equals(user.getId())) {
            throw new ForbiddenException("VISITOR can only cancel their own bookings");
        }
        if (List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            if ("PARK_MANAGER".equals(user.getRole()) && !booking.getPark().getId().equals(user.getPark().getId())) {
                throw new ForbiddenException("PARK_MANAGER can only cancel bookings for their assigned park");
            }
        } else if (!"VISITOR".equals(user.getRole())) {
            throw new ForbiddenException("Only VISITOR, PARK_MANAGER, or ADMIN can cancel bookings");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByVisitor(String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User visitor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"VISITOR".equals(visitor.getRole())) {
            throw new ForbiddenException("Only VISITOR can view their own bookings");
        }
        return bookingRepository.findByVisitorId(visitor.getId());
    }

    public List<Booking> getBookingsByPark(UUID parkId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!List.of("PARK_MANAGER", "ADMIN").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER or ADMIN can view park bookings");
        }
        if ("PARK_MANAGER".equals(user.getRole()) && !parkId.equals(user.getPark().getId())) {
            throw new ForbiddenException("PARK_MANAGER can only view bookings for their assigned park");
        }
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park not found with ID: " + parkId);
        }
        return bookingRepository.findByParkId(parkId);
    }

    public Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));
    }
}