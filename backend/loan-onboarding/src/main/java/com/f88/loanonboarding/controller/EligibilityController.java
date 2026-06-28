package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.response.loan.EligibilityCheckResponse;
import com.f88.loanonboarding.service.EligibilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Eligibility", description = "API kiểm tra điều kiện vay")
@RestController
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @Operation(summary = "Chạy kiểm tra điều kiện vay cho hồ sơ")
    @PostMapping("/api/v1/loan-applications/{applicationCode}/eligibility-checks")
    public ApiResponse<EligibilityCheckResponse> runCheck(@PathVariable String applicationCode) {
        return ApiResponse.success("Eligibility check completed", eligibilityService.runCheck(applicationCode));
    }

    @Operation(summary = "Lấy kết quả kiểm tra điều kiện vay mới nhất")
    @GetMapping("/api/v1/loan-applications/{applicationCode}/eligibility-checks/latest")
    public ApiResponse<EligibilityCheckResponse> getLatest(@PathVariable String applicationCode) {
        return ApiResponse.success(eligibilityService.getLatest(applicationCode));
    }
}
