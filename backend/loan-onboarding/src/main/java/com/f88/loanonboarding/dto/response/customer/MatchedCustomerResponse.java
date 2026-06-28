package com.f88.loanonboarding.dto.response.customer;

import java.time.LocalDate;

public record MatchedCustomerResponse(
        String fullName,
        LocalDate dateOfBirth,
        String identifierNumber,
        String phoneNumber
) {
}
