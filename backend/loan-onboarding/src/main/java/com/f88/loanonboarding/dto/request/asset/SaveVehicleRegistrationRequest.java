package com.f88.loanonboarding.dto.request.asset;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveVehicleRegistrationRequest(
        @NotBlank(message = "So dang ky xe la bat buoc")
        String registrationNumber,

        @NotNull(message = "Ngay cap dang ky xe la bat buoc")
        LocalDate registrationIssueDate
) {
}
