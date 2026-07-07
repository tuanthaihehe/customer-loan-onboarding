package com.f88.loanonboarding.service;

import java.util.List;

import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.UpdateLoanApplicationRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationListItemResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationSummaryResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;

public interface LoanApplicationService {

    LoanApplicationSummaryResponse createApplication(CreateLoanApplicationRequest request);

    List<LoanApplicationListItemResponse> findLoanApplications();

    LoanApplicationDetailResponse getDetail(String applicationCode);

    LoanApplicationSummaryResponse updateLoanRequest(String applicationCode, UpdateLoanApplicationRequest request);

    LoanApplicationSummaryResponse cancel(String applicationCode, CancelLoanApplicationRequest request);

    StepCompletionResponse completePreliminaryStep(String applicationCode);

    SubmitForApprovalResponse submitForApproval(String applicationCode);
}
