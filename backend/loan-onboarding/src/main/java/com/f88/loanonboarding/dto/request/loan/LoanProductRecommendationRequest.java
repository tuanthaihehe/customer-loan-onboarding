package com.f88.loanonboarding.dto.request.loan;

import java.util.List;

import com.f88.loanonboarding.dto.request.asset.ValuationDeductionItemRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LoanProductRecommendationRequest(
        @Valid
        List<ValuationDeductionItemRequest> deductionItems,

        String scoreGrade,

        @Min(value = 1, message = "Số gói vay cần trả về phải lớn hơn 0")
        @Max(value = 10, message = "Số gói vay cần trả về không được vượt quá 10")
        Integer limit
) {
}
