package com.f88.loanonboarding.common.error;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR("ERR_500", "Lỗi hệ thống, vui lòng thử lại sau"),
    VALIDATION_ERROR("ERR_400", "Dữ liệu gửi lên không hợp lệ"),
    RESOURCE_NOT_FOUND("ERR_404", "Không tìm thấy dữ liệu"),
    BUSINESS_RULE_VIOLATION("ERR_BUSINESS_RULE", "Không thỏa điều kiện nghiệp vụ"),
    SCHEMA_NOT_READY("ERR_SCHEMA_NOT_READY", "Database hiện tại chưa hỗ trợ chức năng này"),
    CUSTOMER_NOT_FOUND("CUS_404", "Không tìm thấy khách hàng"),
    CUSTOMER_RESTRICTED("CUS_RESTRICTED", "Khách hàng đang bị hạn chế"),
    INVALID_REQUESTED_AMOUNT("APP_INVALID_AMOUNT", "Số tiền vay phải lớn hơn 0"),
    MISSING_LOAN_PURPOSE("APP_MISSING_PURPOSE", "Mục đích vay là bắt buộc"),
    INVALID_LOAN_PURPOSE("APP_INVALID_PURPOSE", "Mục đích vay không tồn tại trong database"),
    INVALID_LOAN_TERM("APP_INVALID_TERM", "Kỳ hạn vay không tồn tại trong database"),
    LOAN_APPLICATION_NOT_FOUND("APP_404", "Không tìm thấy hồ sơ vay"),
    INVALID_LOAN_APPLICATION_STATE("APP_INVALID_STATE", "Trạng thái hồ sơ vay không hợp lệ"),
    ASSET_ALREADY_PLEDGED("ASSET_ALREADY_PLEDGED", "Tài sản đã được cầm cố"),
    DUPLICATED_ASSET("ASSET_DUPLICATED", "Tài sản đã tồn tại"),
    INVALID_VALUATION_VALUE("VAL_INVALID_VALUE", "Giá trị định giá không hợp lệ"),
    // OCR
    OCR_ID_NOT_FOUND("OCR_ID_NOT_FOUND", "Không phát hiện CCCD/CMND trong ảnh, vui lòng chụp đúng giấy tờ"),
    OCR_IMAGE_BLURRY("OCR_IMAGE_BLURRY", "Ảnh bị mờ hoặc nhòe, vui lòng chụp lại rõ nét hơn"),
    OCR_IMAGE_TOO_FAR("OCR_IMAGE_TOO_FAR", "Ảnh chụp giấy tờ quá xa, vui lòng đưa CCCD/CMND chiếm phần lớn khung hình"),
    OCR_IMAGE_CROP_FAILED("OCR_CROP_FAILED", "Ảnh bị thiếu góc, vui lòng chụp lại toàn bộ giấy tờ"),
    OCR_INVALID_IMAGE("OCR_INVALID_IMAGE", "File không phải ảnh hợp lệ (JPG, PNG, WEBP)"),
    OCR_SERVICE_ERROR("OCR_SERVICE_ERROR", "Lỗi kết nối dịch vụ nhận diện giấy tờ");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
