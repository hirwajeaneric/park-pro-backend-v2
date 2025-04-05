package com.park.parkpro.repository;

import com.park.parkpro.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByVisitorId(UUID visitorId);
    List<Booking> findByParkId(UUID parkId);
}