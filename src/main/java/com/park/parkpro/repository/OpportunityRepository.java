package com.park.parkpro.repository;

import com.park.parkpro.domain.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {
    List<Opportunity> findByVisibility(String visibility);
    List<Opportunity> findByCreatedById(UUID createdById);
    List<Opportunity> findByParkId(UUID parkId);
    List<Opportunity> findByParkIdAndVisibility(UUID parkId, String visibility);
}