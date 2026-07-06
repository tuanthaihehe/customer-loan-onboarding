package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDraft;
import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;

public interface LoanApplicationDraftRepository extends JpaRepository<LoanApplicationDraft, UUID> {

    Optional<LoanApplicationDraft> findByDraftCode(String draftCode);

    boolean existsByDraftCode(String draftCode);

    List<LoanApplicationDraft> findByStatusOrderByUpdatedAtDesc(LoanApplicationDraftStatus status);

    List<LoanApplicationDraft> findAllByOrderByUpdatedAtDesc();
}
