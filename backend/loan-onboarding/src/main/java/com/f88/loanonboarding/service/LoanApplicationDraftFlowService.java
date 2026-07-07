package com.f88.loanonboarding.service;

import java.util.List;

import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationDraftFlowRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftStepRequest;
import com.f88.loanonboarding.dto.request.loan.SubmitLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftSummaryResponse;
import com.f88.loanonboarding.enums.LoanApplicationDraftStatus;

public interface LoanApplicationDraftFlowService {

    LoanApplicationDraftDetailResponse createDraft(CreateLoanApplicationDraftFlowRequest request);

    List<LoanApplicationDraftSummaryResponse> findDrafts(LoanApplicationDraftStatus status);

    LoanApplicationDraftDetailResponse getDraft(String draftCode);

    LoanApplicationDraftStepActionResponse saveStep(
            String draftCode,
            String stepCode,
            SaveLoanApplicationDraftStepRequest request
    );

    LoanApplicationDraftStepActionResponse completeStep(
            String draftCode,
            String stepCode,
            CompleteLoanApplicationDraftStepRequest request
    );

    LoanApplicationDraftSubmitResponse submit(String draftCode, SubmitLoanApplicationDraftRequest request);

    LoanApplicationDraftSubmitResponse cancel(String draftCode, CancelLoanApplicationDraftRequest request);
}
