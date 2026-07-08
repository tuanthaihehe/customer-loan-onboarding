package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;

public record LoanApplicationOnboardingLoanInfoResponse(
        BigDecimal requestedAmount,
        Integer loanTermMonths,
        String branch,
        String currentAddress,
        String workplaceName,
        String workplaceAddress,
        BigDecimal monthlyIncomeAmount,
        String loanPurposeCode,
        String loanPurposeName,
        String loanTermCode,
        String loanTermName,
        String occupationCode,
        String occupationName,
        String incomeSourceCode,
        String incomeSourceName,
        String disbursementBankCode,
        String disbursementBankName,
        String disbursementAccountNumber,
        String disbursementAccountName,
        String loanProductCode,
        String loanProductName,
        BigDecimal loanProductMonthlyInterestRatePercent,
        BigDecimal loanProductMaxLtvPercent
) {
}
