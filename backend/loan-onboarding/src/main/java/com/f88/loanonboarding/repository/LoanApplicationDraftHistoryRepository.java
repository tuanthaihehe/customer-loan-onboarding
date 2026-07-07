package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDraftHistory;

public interface LoanApplicationDraftHistoryRepository extends JpaRepository<LoanApplicationDraftHistory, UUID> {

    List<LoanApplicationDraftHistory> findByDraft_DraftCodeOrderByChangedAtAsc(String draftCode);
}
