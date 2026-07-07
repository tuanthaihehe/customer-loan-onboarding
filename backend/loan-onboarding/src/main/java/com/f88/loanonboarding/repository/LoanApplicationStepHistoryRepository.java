package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationStepHistory;

public interface LoanApplicationStepHistoryRepository extends JpaRepository<LoanApplicationStepHistory, UUID> {

    List<LoanApplicationStepHistory> findByLoanApplication_LoanApplicationCodeOrderByChangedAtAsc(String applicationCode);
}
