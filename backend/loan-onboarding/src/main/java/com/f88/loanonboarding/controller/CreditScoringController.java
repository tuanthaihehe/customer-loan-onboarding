package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.creditscoring.CreditScoringCalculateRequest;
import com.f88.loanonboarding.dto.response.creditscoring.CreditScoringCalculateResponse;
import com.f88.loanonboarding.service.CreditScoringService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Credit Scoring", description = "API tính điểm và hạng điểm khách hàng")
@RestController
@RequestMapping("/api/v1/credit-scoring")
public class CreditScoringController {

    private final CreditScoringService creditScoringService;

    public CreditScoringController(CreditScoringService creditScoringService) {
        this.creditScoringService = creditScoringService;
    }

    @Operation(
            summary = "Tính điểm score khách hàng",
            description = "Tính score theo income, age, dependent count dựa trên các bảng scoring band đang active. Kết quả scoreGrade dùng để gọi lại API đề xuất sản phẩm vay."
    )
    @PostMapping("/calculate")
    public ApiResponse<CreditScoringCalculateResponse> calculate(
            @Valid @RequestBody CreditScoringCalculateRequest request
    ) {
        return ApiResponse.success("Credit score calculated", creditScoringService.calculate(request));
    }
}
