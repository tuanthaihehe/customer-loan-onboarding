package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDraft;
import com.f88.loanonboarding.entity.LoanApplicationDraftStepData;

public interface LoanApplicationDraftStepDataRepository extends JpaRepository<LoanApplicationDraftStepData, UUID> {

    @EntityGraph(attributePaths = "step")
    List<LoanApplicationDraftStepData> findByDraftOrderByStep_StepOrderAsc(LoanApplicationDraft draft);

    @EntityGraph(attributePaths = "step")
    Optional<LoanApplicationDraftStepData> findByDraftAndStep_Code(LoanApplicationDraft draft, String stepCode);

    List<LoanApplicationDraftStepData> findByDraft_DraftCodeOrderByStep_StepOrderAsc(String draftCode);

    Optional<LoanApplicationDraftStepData> findByDraft_DraftCodeAndStep_Code(String draftCode, String stepCode);
}
