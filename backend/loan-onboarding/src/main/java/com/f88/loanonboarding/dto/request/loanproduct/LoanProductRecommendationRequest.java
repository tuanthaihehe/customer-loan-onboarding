package com.f88.loanonboarding.dto.request.loanproduct;

import java.math.BigDecimal;

import com.f88.loanonboarding.enums.AssetType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanProductRecommendationRequest(
        @NotBlank(message = "Mục đích vay là bắt buộc")
        String selectedLoanPurpose,

        @NotNull(message = "Loại tài sản là bắt buộc")
        AssetType selectedAssetType,

        @NotNull(message = "Kỳ hạn vay là bắt buộc")
        @Min(value = 1, message = "Kỳ hạn vay phải từ 1 tháng trở lên")
        Integer selectedTenor,

        @NotNull(message = "Số tiền khách muốn vay là bắt buộc")
        @Positive(message = "Số tiền khách muốn vay phải lớn hơn 0")
        BigDecimal requestedLoanAmount,

        @NotNull(message = "Giá trị tài sản sau định giá là bắt buộc")
        @Positive(message = "Giá trị tài sản sau định giá phải lớn hơn 0")
        BigDecimal adjustedAssetValue,

        String scoreGrade
) {
}
