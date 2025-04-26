package com.park.parkpro.repository;

import com.park.parkpro.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByParkId(UUID parkId);
    Optional<Budget> findByParkIdAndFiscalYear(UUID parkId, Integer fiscalYear);
    List<Budget> findByFiscalYear(Integer fiscalYear);
}