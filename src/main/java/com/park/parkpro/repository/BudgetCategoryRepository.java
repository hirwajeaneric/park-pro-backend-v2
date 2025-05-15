package com.park.parkpro.repository;

import com.park.parkpro.domain.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, UUID> {
    List<BudgetCategory> findByBudgetId(UUID budgetId);

    @Query("SELECT COALESCE(SUM(bc.allocatedAmount), 0) FROM BudgetCategory bc WHERE bc.budget.id = :budgetId")
    BigDecimal sumAllocatedAmountByBudgetId(UUID budgetId);
}