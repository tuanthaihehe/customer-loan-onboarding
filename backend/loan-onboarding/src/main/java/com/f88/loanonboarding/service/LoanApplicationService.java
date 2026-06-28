package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;

public interface LoanApplicationService {

    LoanApplicationDraftResponse createDraft(CreateLoanApplicationRequest request);

    LoanApplicationDetailResponse getDetail(String applicationCode);

    LoanApplicationDraftResponse saveDraft(String applicationCode, SaveLoanApplicationDraftRequest request);

    LoanApplicationDraftResponse cancel(String applicationCode, CancelLoanApplicationRequest request);

    StepCompletionResponse completePreliminaryStep(String applicationCode);

    SubmitForApprovalResponse submitForApproval(String applicationCode);
}
