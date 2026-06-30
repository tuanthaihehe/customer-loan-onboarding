package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationEntity;
import com.f88.loanonboarding.entity.LoanApplicationStateHistoryEntity;

public interface LoanApplicationStateHistoryRepository extends JpaRepository<LoanApplicationStateHistoryEntity, UUID> {

    Optional<LoanApplicationStateHistoryEntity> findTopByLoanApplicationAndActionCodeOrderByChangedAtDesc(
            LoanApplicationEntity loanApplication,
            String actionCode
    );

    Optional<LoanApplicationStateHistoryEntity> findTopByLoanApplicationOrderByChangedAtDesc(
            LoanApplicationEntity loanApplication
    );
}
