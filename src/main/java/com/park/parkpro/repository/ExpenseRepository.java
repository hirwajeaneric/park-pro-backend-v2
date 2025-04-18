package com.park.parkpro.repository;

import com.park.parkpro.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByBudgetCategoryId(UUID budgetCategoryId);
    List<Expense> findByParkId(UUID parkId);
    List<Expense> findByBudgetId(UUID budgetId);
    List<Expense> findByCreatedById(UUID createdById);
}