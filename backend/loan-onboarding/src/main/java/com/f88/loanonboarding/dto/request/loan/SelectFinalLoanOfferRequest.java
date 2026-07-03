package com.f88.loanonboarding.dto.request.loan;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SelectFinalLoanOfferRequest(
        @NotBlank(message = "Mã sản phẩm vay là bắt buộc")
        String productCode,

        @Positive(message = "Số tiền vay cuối cùng phải lớn hơn 0")
        BigDecimal requestedAmount,

        @Min(value = 1, message = "Kỳ hạn vay phải từ 1 tháng trở lên")
        Integer loanTermMonths,

        String paymentMethod,

        @Min(value = 1, message = "Ngày thanh toán hàng tháng phải từ 1 đến 28")
        @Max(value = 28, message = "Ngày thanh toán hàng tháng phải từ 1 đến 28")
        Integer monthlyPaymentDay,

        String processingBranch
) {
}
