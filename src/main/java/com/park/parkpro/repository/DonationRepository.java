package com.park.parkpro.repository;

import com.park.parkpro.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
    List<Donation> findByDonorId(UUID donorId);
    List<Donation> findByParkId(UUID parkId);

    @Query("SELECT d FROM Donation d WHERE d.park.id = :parkId AND d.fiscalYear = :fiscalYear")
    List<Donation> findByParkIdAndFiscalYear(@Param("parkId") UUID parkId, @Param("fiscalYear") int fiscalYear);
}