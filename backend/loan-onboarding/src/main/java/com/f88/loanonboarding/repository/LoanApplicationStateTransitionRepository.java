package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateTransition;

public interface LoanApplicationStateTransitionRepository extends JpaRepository<LoanApplicationStateTransition, UUID> {

    Optional<LoanApplicationStateTransition> findByFromStateAndActionCode(
            LoanApplicationState fromState,
            String actionCode
    );

    boolean existsByFromStateAndToStateAndActionCode(
            LoanApplicationState fromState,
            LoanApplicationState toState,
            String actionCode
    );
}
