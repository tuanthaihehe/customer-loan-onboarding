package com.f88.loanonboarding.dto.request.asset;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveVehicleRegistrationRequest(
        @NotBlank(message = "Số đăng ký xe là bắt buộc")
        String registrationNumber,

        @NotNull(message = "Ngày cấp đăng ký xe là bắt buộc")
        LocalDate registrationIssueDate
) {
}
