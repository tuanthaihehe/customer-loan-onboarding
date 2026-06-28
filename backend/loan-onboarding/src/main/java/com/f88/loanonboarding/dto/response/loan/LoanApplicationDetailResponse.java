package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDateTime;
import java.util.Map;

import com.f88.loanonboarding.enums.LoanApplicationState;

public record LoanApplicationDetailResponse(
        String applicationCode,
        LoanApplicationState applicationState,
        String customerCode,
        Map<String, Object> applicantSnapshot,
        Map<String, Object> loanRequest,
        Map<String, Object> assetSnapshot,
        Map<String, Object> valuationPreview,
        Map<String, Object> stepStatus,
        LocalDateTime updatedDate
) {
}
