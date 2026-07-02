package com.f88.loanonboarding.enums;

public enum AssetStatus {
    AVAILABLE(true, null, null),
    PLEDGED(false, "ASSET_ALREADY_PLEDGED", "Tai san dang duoc cam co, khong the gan vao ho so vay moi."),
    RELEASED(true, null, null),
    SETTLED(true, null, null);

    private final boolean eligibleForPledge;
    private final String blockReasonCode;
    private final String blockReasonMessage;

    AssetStatus(boolean eligibleForPledge, String blockReasonCode, String blockReasonMessage) {
        this.eligibleForPledge = eligibleForPledge;
        this.blockReasonCode = blockReasonCode;
        this.blockReasonMessage = blockReasonMessage;
    }

    public boolean isEligibleForPledge() {
        return eligibleForPledge;
    }

    public String getBlockReasonCode() {
        return blockReasonCode;
    }

    public String getBlockReasonMessage() {
        return blockReasonMessage;
    }
}
