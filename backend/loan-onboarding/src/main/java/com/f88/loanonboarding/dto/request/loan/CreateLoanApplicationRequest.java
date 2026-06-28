package com.f88.loanonboarding.dto.request.loan;

import jakarta.validation.constraints.NotBlank;

public record CreateLoanApplicationRequest(
        @NotBlank(message = "Mã khách hàng là bắt buộc")
        String customerCode,

        @NotBlank(message = "Kênh tiếp nhận là bắt buộc")
        String applicationChannel,

        @NotBlank(message = "Mã chi nhánh là bắt buộc")
        String branchCode,

        @NotBlank(message = "Mã nhân viên là bắt buộc")
        String staffCode
) {
}
