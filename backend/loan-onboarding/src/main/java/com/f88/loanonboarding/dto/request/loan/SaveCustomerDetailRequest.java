package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveCustomerDetailRequest(
        @NotBlank(message = "Giới tính là bắt buộc")
        String gender,

        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Tình trạng hôn nhân là bắt buộc")
        String maritalStatus,

        @NotBlank(message = "Mã nghề nghiệp là bắt buộc")
        String occupationCode,

        @NotBlank(message = "Mã nguồn thu nhập là bắt buộc")
        String incomeSourceCode,

        @NotNull(message = "Thu nhập hàng tháng là bắt buộc")
        @PositiveOrZero(message = "Thu nhập hàng tháng không được âm")
        BigDecimal monthlyIncomeAmount,

        @NotBlank(message = "Mã ngân hàng giải ngân là bắt buộc")
        String disbursementBankCode,

        @NotBlank(message = "Số tài khoản là bắt buộc")
        String disbursementAccountNumber,

        @NotBlank(message = "Chủ tài khoản là bắt buộc")
        String disbursementAccountName,

        String workplaceName,

        String workplaceAddress,

        @NotBlank(message = "Địa chỉ thường trú là bắt buộc")
        String permanentAddress,

        @NotBlank(message = "Địa chỉ hiện tại là bắt buộc")
        String currentAddress
) {
}
