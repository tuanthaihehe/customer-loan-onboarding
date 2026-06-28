package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ApplicantSnapshotRequest(
        @NotBlank(message = "Họ và tên là bắt buộc")
        String fullName,

        @NotNull(message = "Ngày sinh là bắt buộc")
        LocalDate dateOfBirth,

        @NotBlank(message = "Giới tính là bắt buộc")
        String gender,

        @NotBlank(message = "Số CCCD là bắt buộc")
        String identifierNumber,

        @NotBlank(message = "Số điện thoại là bắt buộc")
        String phoneNumber,

        @NotBlank(message = "Nghề nghiệp là bắt buộc")
        String occupation,

        @NotNull(message = "Thu nhập hàng tháng là bắt buộc")
        @Positive(message = "Thu nhập hàng tháng phải lớn hơn 0")
        BigDecimal monthlyIncome
) {
}
