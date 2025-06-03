package com.park.parkpro.repository;

import com.park.parkpro.domain.WithdrawRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, UUID> {
    List<WithdrawRequest> findByBudgetCategoryId(UUID budgetCategoryId);
    List<WithdrawRequest> findByBudgetId(UUID budgetId);
    List<WithdrawRequest> findByRequesterId(UUID requesterId);
    List<WithdrawRequest> findByRequesterIdAndBudgetId(UUID requesterId, UUID budgetId);
    
    @Query("SELECT w FROM WithdrawRequest w WHERE w.park.id = :parkId AND w.budget.fiscalYear = :fiscalYear")
    List<WithdrawRequest> findByParkIdAndYear(@Param("parkId") UUID parkId, @Param("fiscalYear") Integer fiscalYear);
}