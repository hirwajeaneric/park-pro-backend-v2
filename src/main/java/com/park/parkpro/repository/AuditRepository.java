package com.park.parkpro.repository;

import com.park.parkpro.domain.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<Audit, UUID> {
    List<Audit> findByAuditYear(Integer year);
    Optional<Audit> findByParkIdAndAuditYear(UUID parkId, Integer year);
    List<Audit> findByParkId(UUID parkId);
}

