package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record FinalLoanOfferPreviewRequest(
        @Positive(message = "So tien vay cuoi cung phai lon hon 0")
        BigDecimal requestedAmount,

        @Min(value = 1, message = "Ky han vay phai tu 1 thang tro len")
        Integer loanTermMonths,

        String paymentMethod,

        @Min(value = 1, message = "Ngay thanh toan hang thang phai tu 1 den 28")
        @Max(value = 28, message = "Ngay thanh toan hang thang phai tu 1 den 28")
        Integer monthlyPaymentDay,

        String processingBranch,

        @Min(value = 1, message = "So goi vay can tra ve phai lon hon 0")
        @Max(value = 10, message = "So goi vay can tra ve khong duoc vuot qua 10")
        Integer limit
) {
}
