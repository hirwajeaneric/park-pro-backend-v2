package com.park.parkpro.repository;

import com.park.parkpro.domain.IncomeStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncomeStreamRepository extends JpaRepository<IncomeStream, UUID> {
    List<IncomeStream> findByBudgetId(UUID budgetId);
    boolean existsByBudgetIdAndName(UUID budgetId, String name);
}