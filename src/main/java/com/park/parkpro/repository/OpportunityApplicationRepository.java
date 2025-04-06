package com.park.parkpro.repository;

import com.park.parkpro.domain.OpportunityApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OpportunityApplicationRepository extends JpaRepository<OpportunityApplication, UUID> {
    List<OpportunityApplication> findByOpportunityId(UUID opportunityId);
    List<OpportunityApplication> findByEmail(String email);
}