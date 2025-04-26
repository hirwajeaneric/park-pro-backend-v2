package com.park.parkpro.repository;

import com.park.parkpro.domain.OpportunityApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplication, UUID> {
    List<OpportunityApplication> findByOpportunityId(UUID opportunityId);
    List<OpportunityApplication> findByEmail(String email);

    @Query("SELECT oa FROM OpportunityApplication oa JOIN oa.opportunity o WHERE o.park.id = :parkId")
    List<OpportunityApplication> findByParkId(@Param("parkId") UUID parkId);
}