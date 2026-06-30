package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationStateHistory;

public interface LoanApplicationStateHistoryRepository extends JpaRepository<LoanApplicationStateHistory, UUID> {

    Optional<LoanApplicationStateHistory> findFirstByLoanApplicationOrderByChangedAtAsc(LoanApplication loanApplication);

    Optional<LoanApplicationStateHistory> findFirstByLoanApplicationOrderByChangedAtDesc(LoanApplication loanApplication);
}
