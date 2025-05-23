package com.park.parkpro.repository;

import com.park.parkpro.domain.WithdrawRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WithdrawRequestRepository extends JpaRepository<WithdrawRequest, UUID> {
    List<WithdrawRequest> findByBudgetCategoryId(UUID budgetCategoryId);
    List<WithdrawRequest> findByBudgetId(UUID budgetId);
    List<WithdrawRequest> findByRequesterId(UUID requesterId);
    List<WithdrawRequest> findByRequesterIdAndBudgetId(UUID requesterId, UUID budgetId);
}