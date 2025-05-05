package com.park.parkpro.repository;

import com.park.parkpro.domain.FundingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FundingRequestRepository extends JpaRepository<FundingRequest, UUID> {
    List<FundingRequest> findByParkId(UUID parkId);

    @Query("SELECT fr FROM FundingRequest fr JOIN fr.budget b WHERE fr.park.id = :parkId AND b.fiscalYear = :fiscalYear")
    List<FundingRequest> findByParkIdAndFiscalYear(@Param("parkId") UUID parkId, @Param("fiscalYear") int fiscalYear);

    List<FundingRequest> findByBudgetId(UUID budgetId);

    @Query("SELECT fr FROM FundingRequest fr JOIN fr.budget b WHERE b.fiscalYear = :fiscalYear")
    List<FundingRequest> findByFiscalYear(@Param("fiscalYear") int fiscalYear);
}