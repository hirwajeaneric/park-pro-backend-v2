package com.park.parkpro.controller;

import com.park.parkpro.domain.Booking;
import com.park.parkpro.dto.BookingResponseDto;
import com.park.parkpro.dto.CreateBookingRequestDto;
import com.park.parkpro.exception.UnauthorizedException;
import com.park.parkpro.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody CreateBookingRequestDto request,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Booking booking = bookingService.createBooking(request.getActivityId(), request.getVisitDate(), token);
        return ResponseEntity.created(URI.create("/api/bookings/" + booking.getId()))
                .body(mapToBookingDto(booking));
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    public ResponseEntity<BookingResponseDto> confirmBooking(
            @PathVariable UUID bookingId,
            @RequestParam String paymentReference,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        Booking booking = bookingService.confirmBooking(bookingId, paymentReference, token);
        return ResponseEntity.ok(mapToBookingDto(booking));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
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
        List<Booking> bookings = bookingService.getBookingsByPark(parkId, token);
        return ResponseEntity.ok(bookings.stream().map(this::mapToBookingDto).collect(Collectors.toList()));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable UUID bookingId) {
        Booking booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(mapToBookingDto(booking));
    }

    private BookingResponseDto mapToBookingDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(), booking.getVisitor().getId(), booking.getActivity().getId(),
                booking.getAmount(), booking.getPark().getId(), booking.getVisitDate(),
                booking.getStatus(), booking.getPaymentReference(), booking.getCurrency(),
                booking.getConfirmedAt(), booking.getCreatedAt(), booking.getUpdatedAt()
        );
    }
}