package com.park.parkpro.repository;

import com.park.parkpro.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByParkId(UUID parkId);
    Optional<Budget> findByParkIdAndFiscalYear(UUID parkId, Integer fiscalYear);
    List<Budget> findByFiscalYear(Integer fiscalYear);

    @Query("SELECT COALESCE(SUM(bc.balance), 0) FROM BudgetCategory bc WHERE bc.budget.id = :budgetId")
    BigDecimal sumCategoryBalances(UUID budgetId);

    @Query("SELECT b.unallocated FROM Budget b WHERE b.id = :budgetId")
    BigDecimal getUnallocatedById(UUID budgetId);
}