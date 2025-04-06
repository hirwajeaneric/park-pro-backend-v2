package com.park.parkpro.repository;

import com.park.parkpro.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
    List<Donation> findByDonorId(UUID donorId);
    List<Donation> findByParkId(UUID parkId);
}