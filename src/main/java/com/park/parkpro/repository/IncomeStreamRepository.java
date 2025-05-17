package com.park.parkpro.repository;

import com.park.parkpro.domain.IncomeStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeStreamRepository extends JpaRepository<IncomeStream, UUID> {
    List<IncomeStream> findByBudgetId(UUID budgetId);
    List<IncomeStream> findByBudgetIdAndNameContaining(UUID budgetId, String name);
    List<IncomeStream> findByParkIdAndFiscalYear(UUID parkId, Integer fiscalYear);
    @Query("SELECT SUM(percentage) FROM IncomeStream WHERE budget.id = :budgetId")
    Optional<BigDecimal> sumPercentageByBudgetId(@Param("budgetId") UUID budgetId);

    @Query("SELECT SUM(totalContribution) FROM IncomeStream WHERE budget.id = :budgetId")
    Optional<BigDecimal> sumTotalContributionByBudgetId(@Param("budgetId") UUID budgetId);

    @Query("SELECT SUM(percentage) FROM IncomeStream WHERE budget.id = :budgetId AND id != :incomeStreamId")
    Optional<BigDecimal> sumPercentageByBudgetIdExcluding(@Param("budgetId") UUID budgetId, @Param("incomeStreamId") UUID incomeStreamId);

    @Query("SELECT SUM(totalContribution) FROM IncomeStream WHERE budget.id = :budgetId AND id != :incomeStreamId")
    Optional<BigDecimal> sumTotalContributionByBudgetIdExcluding(@Param("budgetId") UUID budgetId, @Param("incomeStreamId") UUID incomeStreamId);

    boolean existsByBudgetIdAndNameContaining(UUID budgetId, String name);
}