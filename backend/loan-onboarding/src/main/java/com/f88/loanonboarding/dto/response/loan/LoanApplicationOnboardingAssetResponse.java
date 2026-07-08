package com.f88.loanonboarding.dto.response.loan;

import java.time.LocalDate;
import java.util.UUID;

public record LoanApplicationOnboardingAssetResponse(
        UUID assetId,
        String assetCode,
        String licensePlate,
        String status,
        String frameNumber,
        String engineNumber,
        String registrationNumber,
        LocalDate registrationIssueDate,
        String vehicleTypeCode,
        String vehicleTypeName,
        String vehicleBrandCode,
        String vehicleBrandName,
        String vehicleModelCode,
        String vehicleModelName,
        String vehicleVersionCode,
        String vehicleVersionName,
        Integer manufactureYear,
        String vehicleColorCode,
        String vehicleColorName,
        String vehicleVariantCode,
        String vehicleVariantName
) {
}
