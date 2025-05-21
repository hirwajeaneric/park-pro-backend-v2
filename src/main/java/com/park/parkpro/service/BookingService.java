package com.park.parkpro.service;

import com.park.parkpro.domain.Activity;
import com.park.parkpro.domain.Booking;
import com.park.parkpro.domain.BookingGroupMember;
import com.park.parkpro.domain.IncomeStream;
import com.park.parkpro.domain.Park;
import com.park.parkpro.domain.User;
import com.park.parkpro.dto.CreateBookingRequestDto;
import com.park.parkpro.exception.BadRequestException;
import com.park.parkpro.exception.ForbiddenException;
import com.park.parkpro.exception.NotFoundException;
import com.park.parkpro.repository.ActivityRepository;
import com.park.parkpro.repository.BookingRepository;
import com.park.parkpro.repository.BudgetRepository;
import com.park.parkpro.repository.IncomeStreamRepository;
import com.park.parkpro.repository.ParkRepository;
import com.park.parkpro.repository.UserRepository;
import com.park.parkpro.security.JwtUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class BookingService {
    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());
    private final BookingRepository bookingRepository;
    private final ActivityRepository activityRepository;
    private final ParkRepository parkRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final IncomeStreamRepository incomeStreamRepository;
    private final JwtUtil jwtUtil;
    private final StripeService stripeService;

    public BookingService(BookingRepository bookingRepository, ActivityRepository activityRepository,
                          ParkRepository parkRepository, UserRepository userRepository,
                          BudgetRepository budgetRepository, IncomeStreamRepository incomeStreamRepository,
                          JwtUtil jwtUtil, StripeService stripeService) {
        this.bookingRepository = bookingRepository;
        this.activityRepository = activityRepository;
        this.parkRepository = parkRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.incomeStreamRepository = incomeStreamRepository;
        this.jwtUtil = jwtUtil;
        this.stripeService = stripeService;
    }

    @Transactional
    public Booking createBooking(UUID activityId, LocalDate visitDate, Integer numberOfTickets,
                                 List<CreateBookingRequestDto.GroupMemberDto> groupMembers,
                                 String paymentMethodId, String token) throws StripeException {
        String email = jwtUtil.getEmailFromToken(token);
        User visitor = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        if (!"VISITOR".equals(visitor.getRole())) {
            throw new ForbiddenException("Only VISITOR can create bookings");
        }
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found with ID: " + activityId));
        Park park = activity.getPark();

        // Validate number of tickets
        if (numberOfTickets < 1) {
            throw new BadRequestException("Number of tickets must be at least 1");
        }
        if (numberOfTickets > 1 && (groupMembers == null || groupMembers.isEmpty())) {
            throw new BadRequestException("Group member details are required for group bookings");
        }
        if (numberOfTickets != (groupMembers != null ? groupMembers.size() + 1 : 1)) {
            throw new BadRequestException("Number of tickets must match the number of group members plus the primary visitor");
        }

        // Availability Check
        if (activity.getCapacityPerDay() != null) {
            long confirmedBookings = bookingRepository.countByActivityIdAndVisitDateAndStatus(
                    activityId, visitDate, "CONFIRMED");
            if (confirmedBookings + numberOfTickets > activity.getCapacityPerDay()) {
                throw new BadRequestException("Not enough available slots for this activity on " + visitDate);
            }
        }
        if (visitDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Visit date must be today or in the future");
        }

        // Validate group members
        if (groupMembers != null) {
            for (CreateBookingRequestDto.GroupMemberDto member : groupMembers) {
                if (member.getUserId() != null) {
                    User memberUser = userRepository.findById(member.getUserId())
                            .orElseThrow(() -> new NotFoundException("User not found with ID: " + member.getUserId()));
                    if (!"VISITOR".equals(memberUser.getRole())) {
                        throw new BadRequestException("Group members must have VISITOR role");
                    }
                } else if (member.getGuestName() == null || member.getGuestEmail() == null) {
                    throw new BadRequestException("Guest name and email are required for non-registered group members");
                }
            }
        }

        // Determine fiscal year and budget
        int fiscalYear = LocalDate.now().getYear();
        var budget = budgetRepository.findByParkIdAndFiscalYear(park.getId(), fiscalYear)
                .orElseThrow(() -> new NotFoundException("No budget found for park " + park.getId() + " and fiscal year " + fiscalYear));

        // Find or create Bookings income stream
        IncomeStream bookingStream = incomeStreamRepository.findByBudgetIdAndNameContaining(budget.getId(), "Bookings")
                .stream().findFirst()
                .orElseGet(() -> {
                    IncomeStream newStream = new IncomeStream();
                    newStream.setBudget(budget);
                    newStream.setPark(park);
                    newStream.setFiscalYear(fiscalYear);
                    newStream.setName("Bookings");
                    newStream.setPercentage(BigDecimal.ZERO);
                    newStream.setTotalContribution(BigDecimal.ZERO);
                    newStream.setActualBalance(BigDecimal.ZERO);
                    newStream.setCreatedBy(visitor);
                    return incomeStreamRepository.save(newStream);
                });

        // Calculate total amount
        BigDecimal totalAmount = activity.getPrice().multiply(new BigDecimal(numberOfTickets));
        Long amountInCents = totalAmount.multiply(new BigDecimal("100")).longValue();

        // Create and confirm PaymentIntent
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                amountInCents, "XAF", "Booking for " + activity.getName() + " (" + numberOfTickets + " tickets)", "temp_booking_id");

        // Attach payment method and confirm
        paymentIntent = paymentIntent.update(Map.of("payment_method", paymentMethodId));
        paymentIntent = stripeService.confirmPaymentIntent(paymentIntent.getId());
        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new BadRequestException("Payment failed: " + paymentIntent.getLastPaymentError().getMessage());
        }

        // Update income stream actual balance
        bookingStream.setActualBalance(bookingStream.getActualBalance().add(totalAmount));
        incomeStreamRepository.save(bookingStream);

        // Create booking
        Booking booking = new Booking();
        booking.setVisitor(visitor);
        booking.setActivity(activity);
        booking.setAmount(totalAmount);
        booking.setPark(park);
        booking.setVisitDate(visitDate);
        booking.setStatus("CONFIRMED");
        booking.setPaymentReference(paymentIntent.getId());
        booking.setStripePaymentIntentId(paymentIntent.getId());
        booking.setStripePaymentStatus(paymentIntent.getStatus());
        booking.setCurrency("XAF");
        booking.setConfirmedAt(LocalDateTime.now());

        // Add primary visitor as a group member
        BookingGroupMember primaryMember = new BookingGroupMember();
        primaryMember.setBooking(booking);
        primaryMember.setUser(visitor);
        booking.getGroupMembers().add(primaryMember);

        // Add additional group members
        if (groupMembers != null) {
            for (CreateBookingRequestDto.GroupMemberDto member : groupMembers) {
                BookingGroupMember groupMember = new BookingGroupMember();
                groupMember.setBooking(booking);
                if (member.getUserId() != null) {
                    User memberUser = userRepository.findById(member.getUserId()).orElseThrow();
                    groupMember.setUser(memberUser);
                } else {
                    groupMember.setGuestName(member.getGuestName());
                    groupMember.setGuestEmail(member.getGuestEmail());
                }
                booking.getGroupMembers().add(groupMember);
            }
        }

        booking = bookingRepository.save(booking);

        // Update metadata with actual booking ID
        paymentIntent.update(Map.of("metadata", Map.of("booking_id", booking.getId().toString())));
        LOGGER.info("Created and confirmed booking: ID=" + booking.getId() + ", Amount=" + booking.getAmount() + ", Tickets=" + numberOfTickets);
        return booking;
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
        if (List.of("PARK_MANAGER", "ADMIN", "FINANCE_OFFICER").contains(user.getRole())) {
            if ("PARK_MANAGER".equals(user.getRole()) && !booking.getPark().getId().equals(user.getPark().getId())) {
                throw new ForbiddenException("PARK_MANAGER can only cancel bookings for their assigned park");
            }
        } else if (!"VISITOR".equals(user.getRole())) {
            throw new ForbiddenException("Only VISITOR, PARK_MANAGER, ADMIN, or FINANCE_OFFICER can cancel bookings");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled");
        }

        // Revert income stream actual balance
        int fiscalYear = LocalDate.now().getYear();
        var budget = budgetRepository.findByParkIdAndFiscalYear(booking.getPark().getId(), fiscalYear)
                .orElseThrow(() -> new NotFoundException("No budget found for park " + booking.getPark().getId() + " and fiscal year " + fiscalYear));
        IncomeStream bookingStream = incomeStreamRepository.findByBudgetIdAndNameContaining(budget.getId(), "Bookings")
                .stream().findFirst()
                .orElseThrow(() -> new NotFoundException("No Bookings income stream found for budget " + budget.getId()));
        bookingStream.setActualBalance(bookingStream.getActualBalance().subtract(booking.getAmount()));
        incomeStreamRepository.save(bookingStream);

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
        if (!List.of("PARK_MANAGER", "ADMIN", "FINANCE_OFFICER", "AUDITOR").contains(user.getRole())) {
            throw new ForbiddenException("Only PARK_MANAGER, ADMIN, FINANCE_OFFICER, or AUDITOR can view park bookings");
        }
        if (!parkRepository.existsById(parkId)) {
            throw new NotFoundException("Park not found with ID: " + parkId);
        }
        List<Booking> bookings = bookingRepository.findByParkId(parkId);
        LOGGER.info("Retrieved " + bookings.size() + " bookings for parkId: " + parkId);
        return bookings;
    }

    public Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));
    }
}