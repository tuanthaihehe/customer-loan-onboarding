package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;

import com.f88.loanonboarding.enums.LoanApplicationState;

public record SubmitForApprovalResponse(
        String applicationCode,
        LoanApplicationState applicationState,
        String approvalCaseCode,
        String eventName,
        LocalDateTime submittedAt,
        String message
) {
}
