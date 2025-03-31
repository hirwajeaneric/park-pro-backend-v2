package com.park.parkpro.repository;

import com.park.parkpro.domain.Park;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ParkRepository extends JpaRepository<Park, UUID> {
    Optional<Park> findByName(String name);

    @Query("SELECT p FROM Park p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Park> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
