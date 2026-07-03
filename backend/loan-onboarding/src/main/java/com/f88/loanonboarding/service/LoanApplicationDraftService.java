package com.f88.loanonboarding.service;

import java.util.UUID;

import com.f88.loanonboarding.dto.request.draft.CreateLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.draft.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftOverviewResponse;
import com.f88.loanonboarding.dto.response.draft.LoanApplicationDraftStepPayloadResponse;
import com.f88.loanonboarding.dto.response.draft.SaveLoanApplicationDraftStepResponse;

public interface LoanApplicationDraftService {

    LoanApplicationDraftOverviewResponse createDraft(CreateLoanApplicationDraftRequest request);

    LoanApplicationDraftOverviewResponse getOverview(UUID draftId);

    LoanApplicationDraftStepPayloadResponse getStepPayload(UUID draftId, String stepCode);

    SaveLoanApplicationDraftStepResponse saveStep(UUID draftId, String stepCode, SaveLoanApplicationDraftStepRequest request);
}
