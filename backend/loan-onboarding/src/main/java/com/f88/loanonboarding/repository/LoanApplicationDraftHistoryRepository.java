package com.f88.loanonboarding.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDraftHistory;

public interface LoanApplicationDraftHistoryRepository extends JpaRepository<LoanApplicationDraftHistory, UUID> {
}
