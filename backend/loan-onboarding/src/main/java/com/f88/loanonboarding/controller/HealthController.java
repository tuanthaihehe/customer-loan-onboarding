package com.f88.loanonboarding.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f88.loanonboarding.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "API kiểm tra trạng thái hệ thống")
@RestController
public class HealthController {

    @Operation(summary = "Kiểm tra hệ thống backend có đang chạy hay không")
    @GetMapping("/api/v1/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Loan Onboarding API is running");
    }
}
