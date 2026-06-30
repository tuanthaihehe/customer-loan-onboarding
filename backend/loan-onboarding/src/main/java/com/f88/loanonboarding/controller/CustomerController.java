package com.f88.loanonboarding.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.common.response.ApiResponse;
import com.f88.loanonboarding.dto.request.customer.CreateCustomerRequest;
import com.f88.loanonboarding.dto.request.customer.CustomerLookupRequest;
import com.f88.loanonboarding.dto.response.customer.CreatedCustomerResponse;
import com.f88.loanonboarding.dto.response.customer.CustomerLookupResponse;
import com.f88.loanonboarding.dto.response.customer.OcrExtractResponse;
import com.f88.loanonboarding.service.CustomerService;
import com.f88.loanonboarding.service.OcrService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Customer", description = "API định danh và tra cứu khách hàng")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final OcrService ocrService;

    public CustomerController(CustomerService customerService, OcrService ocrService) {
        this.customerService = customerService;
        this.ocrService = ocrService;
    }

    @Operation(summary = "Tra cứu khách hàng trước khi tạo hồ sơ vay")
    @PostMapping("/lookup")
    public ApiResponse<CustomerLookupResponse> lookup(@Valid @RequestBody CustomerLookupRequest request) {
        return ApiResponse.success("Tra cứu khách hàng thành công", customerService.lookup(request));
    }

    @Operation(summary = "Tạo khách hàng mới khi tra cứu không tìm thấy")
    @PostMapping
    public ApiResponse<CreatedCustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.success("Tạo khách hàng thành công", customerService.create(request));
    }

    @Operation(
            summary = "Nhận diện CCCD/CMT qua OCR — tự động điền thông tin định danh",
            description = "Upload ảnh mặt trước (bắt buộc) và mặt sau (không bắt buộc) của CCCD/CMT. "
            + "Hệ thống gọi FPT AI để trích xuất: họ tên, ngày sinh, số giấy tờ. "
            + "Số điện thoại không có trong OCR, Staff cần nhập thủ công."
    )
    @PostMapping(value = "/ocr/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OcrExtractResponse> extractOcr(
            @RequestPart("frontImage") MultipartFile frontImage,
            @RequestPart(value = "backImage", required = false) MultipartFile backImage
    ) {
        return ApiResponse.success("Trích xuất OCR thành công", ocrService.extract(frontImage, backImage));
    }
}
