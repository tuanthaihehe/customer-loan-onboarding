package com.f88.loanonboarding.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.f88.loanonboarding.dto.response.loan.LoanApplicationDetailResponse;
import com.f88.loanonboarding.dto.response.loan.LoanApplicationDraftResponse;
import com.f88.loanonboarding.dto.response.loan.StepCompletionResponse;
import com.f88.loanonboarding.dto.response.loan.SubmitForApprovalResponse;
import com.f88.loanonboarding.enums.LoanApplicationState;

@Component
public class DemoLoanApplicationMockDataProvider {

    public static final String DEMO_APPLICATION_CODE = "APP-2026-000001";
    public static final String DEMO_APPROVAL_CASE_CODE = "APR-2026-000001";

    public LoanApplicationDraftResponse draft(String customerCode) {
        LocalDateTime now = LocalDateTime.now();
        return new LoanApplicationDraftResponse(
                DEMO_APPLICATION_CODE,
                LoanApplicationState.APP_DRAFT,
                customerCode,
                now,
                now
        );
    }

    public LoanApplicationDraftResponse savedDraft(String applicationCode) {
        return new LoanApplicationDraftResponse(
                applicationCode,
                LoanApplicationState.APP_DRAFT,
                DemoCustomerMockDataProvider.DEMO_CUSTOMER_CODE,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now()
        );
    }

    public LoanApplicationDraftResponse cancelled(String applicationCode) {
        return new LoanApplicationDraftResponse(
                applicationCode,
                LoanApplicationState.APP_CANCELLED,
                DemoCustomerMockDataProvider.DEMO_CUSTOMER_CODE,
                LocalDateTime.now().minusMinutes(20),
                LocalDateTime.now()
        );
    }

    public StepCompletionResponse preliminaryStepCompleted(String applicationCode) {
        return new StepCompletionResponse(
                applicationCode,
                "PRELIMINARY_INFO",
                true,
                "ASSET_INFO",
                List.of()
        );
    }

    public SubmitForApprovalResponse submittedForApproval(String applicationCode) {
        return new SubmitForApprovalResponse(
                applicationCode,
                LoanApplicationState.APP_SUBMITTED,
                DEMO_APPROVAL_CASE_CODE,
                "LoanApplicationSubmittedForApproval",
                LocalDateTime.now(),
                "Demo flow completed: loan application is ready for approval review."
        );
    }

    public LoanApplicationDetailResponse detail(String applicationCode) {
        return new LoanApplicationDetailResponse(
                applicationCode,
                LoanApplicationState.APP_DRAFT,
                DemoCustomerMockDataProvider.DEMO_CUSTOMER_CODE,
                Map.of(
                        "fullName", "Nguyen Van A",
                        "dateOfBirth", "2003-03-09",
                        "identifierNumber", "123544353234",
                        "phoneNumber", "0918254354"
                ),
                Map.of(
                        "loanPurpose", "BUSINESS",
                        "requestedAmount", BigDecimal.valueOf(20_000_000),
                        "requestedTenure", 12
                ),
                Map.of(
                        "assetType", "MOTORBIKE",
                        "licensePlate", "29A12345",
                        "brand", "HONDA",
                        "model", "SH"
                ),
                Map.of(
                        "marketValue", BigDecimal.valueOf(30_000_000),
                        "finalValue", BigDecimal.valueOf(27_900_000),
                        "loanableValue", BigDecimal.valueOf(19_530_000)
                ),
                Map.of(
                        "IDENTIFY_CUSTOMER", "COMPLETED",
                        "PRELIMINARY_INFO", "COMPLETED",
                        "ASSET_INFO", "COMPLETED",
                        "VALUATION_PREVIEW", "COMPLETED",
                        "ELIGIBILITY_CHECK", "PASSED",
                        "SUBMIT_FOR_APPROVAL", "READY"
                ),
                LocalDateTime.now()
        );
    }
}
