package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDraft;

public interface LoanApplicationDraftRepository extends JpaRepository<LoanApplicationDraft, UUID> {

    boolean existsByDraftCode(String draftCode);

    Optional<LoanApplicationDraft> findTopByDraftCodeStartingWithOrderByDraftCodeDesc(String prefix);

    @EntityGraph(attributePaths = {
            "customer",
            "currentStep"
    })
    Optional<LoanApplicationDraft> findById(UUID id);
}
