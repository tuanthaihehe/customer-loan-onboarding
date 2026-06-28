package com.f88.loanonboarding.mock;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class DemoValuationMockDataProvider {

    public BigDecimal defaultMarketValue() {
        return BigDecimal.valueOf(30_000_000);
    }

    public BigDecimal defaultLtvRatio() {
        return BigDecimal.valueOf(70);
    }
}
