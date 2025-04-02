package com.park.parkpro.repository;

import com.park.parkpro.domain.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, UUID> {
    List<BudgetCategory> findByBudgetId(UUID budgetId);
    boolean existsByIdAndUsedAmountGreaterThan(UUID id, BigDecimal usedAmount);
}