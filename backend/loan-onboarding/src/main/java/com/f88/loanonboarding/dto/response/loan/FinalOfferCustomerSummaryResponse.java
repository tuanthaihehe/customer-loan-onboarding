package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDate;

public record FinalOfferCustomerSummaryResponse(
        String customerCode,
        String fullName,
        String identifierNumber,
        String phoneNumber,
        LocalDate dateOfBirth,
        String customerStatus
) {
}
