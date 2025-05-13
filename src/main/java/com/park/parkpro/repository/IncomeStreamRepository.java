package com.park.parkpro.repository;

import com.park.parkpro.domain.IncomeStream;
import com.park.parkpro.domain.Park;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IncomeStreamRepository extends JpaRepository<IncomeStream, UUID> {
    List<IncomeStream> findByBudgetId(UUID budgetId);
    boolean existsByBudgetIdAndName(UUID budgetId, String name);
    List<IncomeStream> findByParkAndFiscalYear(Park park, Integer fiscalYear);
    @Query("SELECT is FROM IncomeStream is WHERE is.budget.id = :budgetId AND is.fiscalYear = :fiscalYear")
    List<IncomeStream> findByBudgetIdAndFiscalYear(@Param("budgetId") UUID budgetId, @Param("fiscalYear") int fiscalYear);
}