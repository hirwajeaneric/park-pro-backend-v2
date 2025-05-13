package com.park.parkpro.repository;

import com.park.parkpro.domain.Donation;
import com.park.parkpro.dto.OutstandingDonorResponseDto;
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

    @Query("SELECT new com.park.parkpro.dto.OutstandingDonorResponseDto(" +
            "d.donor.id, CONCAT(d.donor.firstName, ' ', d.donor.lastName), SUM(d.amount), " +
            "(SELECT d2.motiveForDonation FROM Donation d2 WHERE d2.donor.id = d.donor.id AND d2.park.id = :parkId " +
            "AND d2.createdAt = (SELECT MAX(d3.createdAt) FROM Donation d3 WHERE d3.donor.id = d.donor.id AND d3.park.id = :parkId))) " +
            "FROM Donation d " +
            "WHERE d.park.id = :parkId " +
            "GROUP BY d.donor.id, d.donor.firstName, d.donor.lastName " +
            "ORDER BY SUM(d.amount) DESC " +
            "FETCH FIRST 8 ROWS ONLY")
    List<OutstandingDonorResponseDto> findTopDonorsByPark(@Param("parkId") UUID parkId);
}