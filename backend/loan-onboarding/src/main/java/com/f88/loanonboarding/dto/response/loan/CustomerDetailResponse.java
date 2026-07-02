package com.f88.loanonboarding.dto.response.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerDetailResponse(
        String applicationCode,
        String customerCode,
        String fullName,
        String identifierNumber,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String maritalStatus,
        String occupationCode,
        String occupationName,
        String incomeSourceCode,
        String incomeSourceName,
        BigDecimal monthlyIncomeAmount,
        String disbursementBankCode,
        String disbursementBankName,
        String disbursementAccountNumber,
        String disbursementAccountName,
        String workplaceName,
        String workplaceAddress,
        String permanentAddress,
        String currentAddress
) {
}
