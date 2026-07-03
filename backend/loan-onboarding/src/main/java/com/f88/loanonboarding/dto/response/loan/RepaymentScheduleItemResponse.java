package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;

public record RepaymentScheduleItemResponse(
        Integer period,
        BigDecimal beginningBalance,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal totalPaymentAmount,
        BigDecimal endingBalance
) {
}
