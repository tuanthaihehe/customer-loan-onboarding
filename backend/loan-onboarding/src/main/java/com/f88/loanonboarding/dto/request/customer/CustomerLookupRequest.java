package com.f88.loanonboarding.dto.request.customer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerLookupRequest(
        @NotBlank(message = "Họ và tên là bắt buộc")
        String fullName,

        @NotNull(message = "Ngày sinh là bắt buộc")
        LocalDate dateOfBirth,

        @NotBlank(message = "Loại giấy tờ là bắt buộc")
        String identifierType,

        @NotBlank(message = "Số giấy tờ là bắt buộc")
        String identifierNumber,

        @NotBlank(message = "Số điện thoại là bắt buộc")
        String phoneNumber
) {
}
