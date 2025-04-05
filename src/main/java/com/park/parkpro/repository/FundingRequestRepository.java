package com.park.parkpro.repository;

import com.park.parkpro.domain.FundingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FundingRequestRepository extends JpaRepository<FundingRequest, UUID> {
    List<FundingRequest> findByParkId(UUID parkId);
}