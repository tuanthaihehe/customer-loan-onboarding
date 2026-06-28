package com.f88.loanonboarding.common.error;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR("ERR_500", "Internal server error"),
    VALIDATION_ERROR("ERR_400", "Validation error"),
    RESOURCE_NOT_FOUND("ERR_404", "Resource not found"),
    BUSINESS_RULE_VIOLATION("ERR_BUSINESS_RULE", "Business rule violation"),
    CUSTOMER_NOT_FOUND("CUS_404", "Customer not found"),
    CUSTOMER_RESTRICTED("CUS_RESTRICTED", "Customer is restricted"),
    INVALID_REQUESTED_AMOUNT("APP_INVALID_AMOUNT", "Requested amount must be greater than zero"),
    MISSING_LOAN_PURPOSE("APP_MISSING_PURPOSE", "Loan purpose is required"),
    LOAN_APPLICATION_NOT_FOUND("APP_404", "Loan application not found"),
    INVALID_LOAN_APPLICATION_STATE("APP_INVALID_STATE", "Invalid loan application state"),
    ASSET_ALREADY_PLEDGED("ASSET_ALREADY_PLEDGED", "Asset is already pledged"),
    DUPLICATED_ASSET("ASSET_DUPLICATED", "Asset already exists"),
    INVALID_VALUATION_VALUE("VAL_INVALID_VALUE", "Invalid valuation value");

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
