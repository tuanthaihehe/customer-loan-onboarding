package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SaveCustomerDetailRequest(
        @NotBlank(message = "Gioi tinh la bat buoc")
        String gender,

        @Email(message = "Email khong dung dinh dang")
        String email,

        @NotBlank(message = "Tinh trang hon nhan la bat buoc")
        String maritalStatus,

        @NotBlank(message = "Ma nghe nghiep la bat buoc")
        String occupationCode,

        @NotBlank(message = "Ma nguon thu nhap la bat buoc")
        String incomeSourceCode,

        @NotNull(message = "Thu nhap hang thang la bat buoc")
        @PositiveOrZero(message = "Thu nhap hang thang khong duoc am")
        BigDecimal monthlyIncomeAmount,

        @NotBlank(message = "Ma ngan hang giai ngan la bat buoc")
        String disbursementBankCode,

        @NotBlank(message = "So tai khoan la bat buoc")
        String disbursementAccountNumber,

        @NotBlank(message = "Chu tai khoan la bat buoc")
        String disbursementAccountName,

        String workplaceName,

        String workplaceAddress,

        @NotBlank(message = "Dia chi thuong tru la bat buoc")
        String permanentAddress,

        @NotBlank(message = "Dia chi hien tai la bat buoc")
        String currentAddress
) {
}
