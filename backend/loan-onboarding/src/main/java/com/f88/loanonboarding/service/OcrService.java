package com.f88.loanonboarding.service;

import org.springframework.web.multipart.MultipartFile;

import com.f88.loanonboarding.dto.response.customer.OcrExtractResponse;

public interface OcrService {

    /**
     * Nhận diện thông tin CCCD/CMT từ ảnh upload. frontImage bắt buộc,
     * backImage không bắt buộc. Dữ liệu trả về dùng để tự động điền form định
     * danh KH ở WF-01.
     */
    OcrExtractResponse extract(MultipartFile frontImage, MultipartFile backImage);
}
