package com.f88.loanonboarding.mock;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.f88.loanonboarding.dto.response.loan.EligibilityCheckResponse;
import com.f88.loanonboarding.enums.EligibilityResult;

@Component
public class DemoEligibilityMockDataProvider {

    public EligibilityCheckResponse passed(String applicationCode) {
        return new EligibilityCheckResponse(
                "ELG-000001",
                applicationCode,
                "CHECKLIST_PASSED",
                EligibilityResult.PASSED,
                8,
                8,
                0,
                0,
                LocalDateTime.now()
        );
    }
}
