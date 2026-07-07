package com.f88.loanonboarding.service;

import java.util.List;

import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationOnboardingRequest;
import com.f88.loanonboarding.dto.request.loan.CompleteLoanApplicationStepRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationOnboardingRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationStepActionResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSubmitResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationOnboardingSummaryResponse;
import com.f88.loanonboarding.enums.LoanApplicationOnboardingStatus;

public interface LoanApplicationOnboardingService {

    LoanApplicationOnboardingDetailResponse createApplication(CreateLoanApplicationOnboardingRequest request);

    List<LoanApplicationOnboardingSummaryResponse> findApplications(LoanApplicationOnboardingStatus status);

    LoanApplicationOnboardingDetailResponse getApplication(String applicationCode);

    LoanApplicationStepActionResponse completeStep(
            String applicationCode,
            String stepCode,
            CompleteLoanApplicationStepRequest request
    );

    LoanApplicationSubmitResponse submit(String applicationCode);

    LoanApplicationSubmitResponse cancel(String applicationCode, CancelLoanApplicationOnboardingRequest request);
}
