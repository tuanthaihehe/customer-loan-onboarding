package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDate;
import java.util.UUID;

public record LoanApplicationDraftCustomerResponse(
        UUID customerId,
        String customerCode,
        String fullName,
        String phoneNumber,
        String identityNumber,
        LocalDate dateOfBirth
) {
}
