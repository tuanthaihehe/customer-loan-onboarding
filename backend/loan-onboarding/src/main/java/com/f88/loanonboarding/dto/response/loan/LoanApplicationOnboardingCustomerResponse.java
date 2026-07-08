package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDate;
import java.util.UUID;

public record LoanApplicationOnboardingCustomerResponse(
        UUID customerId,
        String customerCode,
        String fullName,
        String phoneNumber,
        String identityNumber,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String maritalStatus,
        String permanentAddress,
        String status
) {
}
