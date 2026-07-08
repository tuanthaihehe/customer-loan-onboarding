package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.f88.loanonboarding.enums.LoanApplicationState;

public record LoanApplicationListItemResponse(
        String applicationCode,
        LoanApplicationState applicationState,
        String applicationStateName,
        String customerCode,
        String customerName,
        String phoneNumber,
        String identityNumber,
        BigDecimal requestedAmount,
        Integer loanTermMonths,
        String loanPurposeCode,
        String loanPurposeName,
        String loanProductCode,
        String loanProductName,
        String branch,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
