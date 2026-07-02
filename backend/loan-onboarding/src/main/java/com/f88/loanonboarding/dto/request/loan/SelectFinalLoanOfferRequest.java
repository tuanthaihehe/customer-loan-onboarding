package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SelectFinalLoanOfferRequest(
        @NotBlank(message = "Ma san pham vay la bat buoc")
        String productCode,

        @Positive(message = "So tien vay cuoi cung phai lon hon 0")
        BigDecimal requestedAmount,

        @Min(value = 1, message = "Ky han vay phai tu 1 thang tro len")
        Integer loanTermMonths,

        String paymentMethod,

        @Min(value = 1, message = "Ngay thanh toan hang thang phai tu 1 den 28")
        @Max(value = 28, message = "Ngay thanh toan hang thang phai tu 1 den 28")
        Integer monthlyPaymentDay,

        String processingBranch
) {
}
