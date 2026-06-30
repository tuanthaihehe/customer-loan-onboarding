package com.f88.loanonboarding.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationState;
import com.f88.loanonboarding.entity.LoanApplicationStateTransition;

public interface LoanApplicationStateTransitionRepository extends JpaRepository<LoanApplicationStateTransition, UUID> {

    boolean existsByFromStateAndToStateAndActionCode(
            LoanApplicationState fromState,
            LoanApplicationState toState,
            String actionCode
    );
}
