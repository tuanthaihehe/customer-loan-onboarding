package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.response.loan.EligibilityCheckResponse;

public interface EligibilityService {

    EligibilityCheckResponse runCheck(String applicationCode);

    EligibilityCheckResponse getLatest(String applicationCode);
}
