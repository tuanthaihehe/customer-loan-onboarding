package com.f88.loanonboarding.dto.request.loan;

import jakarta.validation.constraints.NotBlank;

public record CancelLoanApplicationRequest(
        @NotBlank(message = "Lý do hủy hồ sơ là bắt buộc")
        String cancellationReasonCode,

        String note
) {
}
