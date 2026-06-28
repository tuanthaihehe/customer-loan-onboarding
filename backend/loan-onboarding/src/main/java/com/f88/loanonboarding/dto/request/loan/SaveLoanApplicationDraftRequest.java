package com.f88.loanonboarding.dto.request.loan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SaveLoanApplicationDraftRequest(
        @Valid
        @NotNull(message = "Thông tin khách hàng trong hồ sơ là bắt buộc")
        ApplicantSnapshotRequest applicantSnapshot,

        @Valid
        @NotNull(message = "Thông tin khoản vay là bắt buộc")
        LoanRequestData loanRequest
) {
}
