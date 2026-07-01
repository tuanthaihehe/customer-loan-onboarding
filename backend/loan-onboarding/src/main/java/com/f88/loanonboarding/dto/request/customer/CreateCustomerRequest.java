package com.f88.loanonboarding.dto.request.customer;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerRequest(
        @NotBlank(message = "Họ và tên là bắt buộc")
        String fullName,

        @NotBlank(message = "Số giấy tờ là bắt buộc")
        String identifierNumber,

        @NotBlank(message = "Số điện thoại là bắt buộc")
        String phoneNumber,

        @NotNull(message = "Ngày sinh là bắt buộc")
        LocalDate dateOfBirth
) {
}
