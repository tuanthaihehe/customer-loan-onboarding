package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDraft;

public interface LoanApplicationDraftRepository extends JpaRepository<LoanApplicationDraft, UUID> {

    Optional<LoanApplicationDraft> findByDraftCode(String draftCode);

    boolean existsByDraftCode(String draftCode);
}
