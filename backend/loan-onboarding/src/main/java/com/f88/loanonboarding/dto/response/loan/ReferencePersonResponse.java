package com.f88.loanonboarding.dto.response.loan;

public record ReferencePersonResponse(
        String fullName,
        String phoneNumber,
        String relationshipType,
        String address,
        String note
) {
}
