package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationStateEntity;
import com.f88.loanonboarding.entity.LoanApplicationStateTransitionEntity;

public interface LoanApplicationStateTransitionRepository extends JpaRepository<LoanApplicationStateTransitionEntity, UUID> {

    Optional<LoanApplicationStateTransitionEntity> findByFromStateAndActionCode(
            LoanApplicationStateEntity fromState,
            String actionCode
    );
}
