package com.park.parkpro.repository;

import com.park.parkpro.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByVisitorId(UUID visitorId);
    List<Booking> findByParkId(UUID parkId);

    @Query("SELECT b FROM Booking b JOIN b.groupMembers gm WHERE gm.user.id = :userId")
    List<Booking> findByGroupMemberUserId(UUID userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.activity.id = :activityId AND b.visitDate = :visitDate AND b.status = 'CONFIRMED'")
    long countByActivityIdAndVisitDateAndStatus(UUID activityId, LocalDate visitDate, String status);
}