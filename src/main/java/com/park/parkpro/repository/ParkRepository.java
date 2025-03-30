package com.park.parkpro.repository;

import com.park.parkpro.domain.Park;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParkRepository extends JpaRepository<Park, UUID> {
    Optional<Park> findByName(String name);
}
