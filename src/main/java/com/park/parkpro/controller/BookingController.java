package com.park.parkpro.controller;

import com.park.parkpro.domain.Booking;
import com.park.parkpro.dto.BookingResponseDto;
import com.park.parkpro.dto.CreateBookingRequestDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BookingService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {
    private static final Logger LOGGER = Logger.getLogger(BookingController.class.getName());
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody CreateBookingRequestDto request,
            @RequestParam String paymentMethodId,
            @RequestHeader("Authorization") String authHeader) throws StripeException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Creating booking for activityId: " + request.getActivityId());
        Booking booking = bookingService.createBooking(
                request.getActivityId(), request.getVisitDate(), request.getNumberOfTickets(),
                request.getGroupMembers(), paymentMethodId, token);
        return ResponseEntity.created(URI.create("/api/bookings/" + booking.getId()))
                .body(mapToBookingDto(booking));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Cancelling booking: " + bookingId);
        Booking booking = bookingService.cancelBooking(bookingId, token);
        return ResponseEntity.ok(mapToBookingDto(booking));
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching bookings for visitor");
        List<Booking> bookings = bookingService.getBookingsByVisitor(token);
        return ResponseEntity.ok(bookings.stream().map(this::mapToBookingDto).collect(Collectors.toList()));
    }

    @GetMapping("/parks/{parkId}/bookings")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByPark(
            @PathVariable UUID parkId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        LOGGER.info("Fetching bookings for parkId: " + parkId);
        List<Booking> bookings = bookingService.getBookingsByPark(parkId, token);
        return ResponseEntity.ok(bookings.stream().map(this::mapToBookingDto).collect(Collectors.toList()));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable UUID bookingId) {
        LOGGER.info("Fetching booking: " + bookingId);
        Booking booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(mapToBookingDto(booking));
    }

    private BookingResponseDto mapToBookingDto(Booking booking) {
        List<BookingResponseDto.GroupMemberResponseDto> groupMembers = booking.getGroupMembers().stream()
                .map(gm -> new BookingResponseDto.GroupMemberResponseDto(
                        gm.getUser() != null ? gm.getUser().getId() : null,
                        gm.getGuestName(),
                        gm.getGuestEmail()))
                .collect(Collectors.toList());

        return new BookingResponseDto(
                booking.getId(), booking.getVisitor().getId(), booking.getActivity().getId(),
                booking.getAmount(), booking.getPark().getId(), booking.getVisitDate(),
                booking.getStatus(), booking.getPaymentReference(), booking.getCurrency(),
                booking.getConfirmedAt(), booking.getCreatedAt(), booking.getUpdatedAt(),
                booking.getGroupMembers().size(), groupMembers
        );
    }
}