package com.f88.loanonboarding.dto.response.customer;

public record OcrExtractResponse(
        // Trường từ mặt trước CCCD/CMT — tự động điền vào form định danh
        String fullName,
        String dateOfBirth, // dd/MM/yyyy
        String identityNumber,
        // Thông tin bổ sung từ mặt trước
        String documentType, // cccd_12_front | cmnd_12_front | cmnd_09_front
        String sex,
        String nationality,
        String expiryDate, // null nếu là CMT cũ không có ngày hết hạn

        // Thông tin từ mặt sau (nếu có upload)
        String issueDate,
        // Trạng thái xử lý
        boolean frontImageProcessed,
        boolean backImageProcessed
        ) {

}
