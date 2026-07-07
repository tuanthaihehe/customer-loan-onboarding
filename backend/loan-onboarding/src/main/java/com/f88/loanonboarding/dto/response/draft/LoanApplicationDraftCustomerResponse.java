package com.f88.loanonboarding.dto.response.draft;

import java.time.LocalDate;
import java.util.UUID;

public record LoanApplicationDraftCustomerResponse(
        UUID customerId,
        String customerCode,
        String fullName,
        String identityNumber,
        String phoneNumber,
        LocalDate dateOfBirth,
        String status
) {
}
