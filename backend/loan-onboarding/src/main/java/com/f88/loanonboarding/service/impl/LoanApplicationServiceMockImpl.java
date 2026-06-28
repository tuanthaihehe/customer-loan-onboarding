package com.f88.loanonboarding.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.request.loan.CancelLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.CreateLoanApplicationRequest;
import com.f88.loanonboarding.dto.request.loan.SaveLoanApplicationDraftRequest;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;
import com.f88.loanonboarding.mock.DemoLoanApplicationMockDataProvider;
import com.f88.loanonboarding.rule.RuleContext;
import com.f88.loanonboarding.rule.RuleEvaluationService;
import com.f88.loanonboarding.rule.loan.LoanPurposeRule;
import com.f88.loanonboarding.rule.loan.LoanTenureRule;
import com.f88.loanonboarding.rule.loan.RequestedAmountRule;
import com.f88.loanonboarding.service.LoanApplicationService;

@Service
public class LoanApplicationServiceMockImpl implements LoanApplicationService {

    private final RuleEvaluationService ruleEvaluationService;
    private final DemoLoanApplicationMockDataProvider mockDataProvider;

    public LoanApplicationServiceMockImpl(
            RuleEvaluationService ruleEvaluationService,
            DemoLoanApplicationMockDataProvider mockDataProvider
    ) {
        this.ruleEvaluationService = ruleEvaluationService;
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public LoanApplicationDraftResponse createDraft(CreateLoanApplicationRequest request) {
        return mockDataProvider.draft(request.customerCode());
    }

    @Override
    public LoanApplicationDetailResponse getDetail(String applicationCode) {
        return mockDataProvider.detail(applicationCode);
    }

    @Override
    public LoanApplicationDraftResponse saveDraft(String applicationCode, SaveLoanApplicationDraftRequest request) {
        ruleEvaluationService.validateOrThrow(
                RuleContext.loan(
                        request.loanRequest().requestedAmount(),
                        request.loanRequest().requestedTenure(),
                        request.loanRequest().loanPurpose()
                ),
                List.of(new RequestedAmountRule(), new LoanTenureRule(), new LoanPurposeRule())
        );

        return mockDataProvider.savedDraft(applicationCode);
    }

    @Override
    public LoanApplicationDraftResponse cancel(String applicationCode, CancelLoanApplicationRequest request) {
        return mockDataProvider.cancelled(applicationCode);
    }

    @Override
    public StepCompletionResponse completePreliminaryStep(String applicationCode) {
        return mockDataProvider.preliminaryStepCompleted(applicationCode);
    }

    @Override
    public SubmitForApprovalResponse submitForApproval(String applicationCode) {
        // Demo guard only: in the current scope, this endpoint represents the end of Flow 1.
        // Final approval rules will be implemented after BA rules and ERD are finalized.
        return mockDataProvider.submittedForApproval(applicationCode);
    }
}
