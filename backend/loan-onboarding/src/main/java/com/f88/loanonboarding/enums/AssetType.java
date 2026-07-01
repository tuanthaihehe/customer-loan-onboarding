package com.f88.loanonboarding.enums;

public enum AssetType {
    MOTORBIKE,
    CAR;

    public String code() {
        return name();
    }

    public static AssetType fromCode(String code) {
        return AssetType.valueOf(code);
    }
}
