package com.park.parkpro.repository;

import com.park.parkpro.domain.Expense;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByBudgetCategoryId(UUID budgetCategoryId);
    List<Expense> findByParkId(UUID parkId);
    List<Expense> findByBudgetId(UUID budgetId);
    List<Expense> findByCreatedById(UUID createdById);
    List<Expense> findByCreatedByIdAndBudgetId(UUID createdById, UUID budgetId);
    
    @Query("SELECT e FROM Expense e WHERE e.park.id = :parkId AND e.budget.fiscalYear = :fiscalYear")
    List<Expense> findByParkIdAndYear(
        @NotNull(message = "Park ID is required") @Param("parkId") UUID parkId, 
        @NotNull(message = "Audit year is required") @Param("fiscalYear") Integer fiscalYear
    );
}