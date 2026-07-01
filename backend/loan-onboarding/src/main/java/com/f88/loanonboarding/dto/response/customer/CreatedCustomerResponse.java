package com.f88.loanonboarding.dto.response.customer;

import java.time.LocalDate;

public record CreatedCustomerResponse(
        String customerCode,
        String fullName,
        String identifierNumber,
        String phoneNumber,
        LocalDate dateOfBirth,
        String status
) {
}
