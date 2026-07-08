package com.f88.loanonboarding.dto.response.loan;

import java.util.UUID;

public record LoanApplicationOnboardingReferencePersonResponse(
        UUID referencePersonId,
        String fullName,
        String phoneNumber,
        String address,
        String relationshipType,
        String note
) {
}
