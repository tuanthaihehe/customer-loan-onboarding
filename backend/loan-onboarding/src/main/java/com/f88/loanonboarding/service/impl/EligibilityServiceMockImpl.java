package com.f88.loanonboarding.service.impl;

import org.springframework.stereotype.Service;

import com.f88.loanonboarding.dto.response.loan.EligibilityCheckResponse;
import com.f88.loanonboarding.mock.DemoEligibilityMockDataProvider;
import com.f88.loanonboarding.service.EligibilityService;

@Service
public class EligibilityServiceMockImpl implements EligibilityService {

    private final DemoEligibilityMockDataProvider mockDataProvider;

    public EligibilityServiceMockImpl(DemoEligibilityMockDataProvider mockDataProvider) {
        this.mockDataProvider = mockDataProvider;
    }

    @Override
    public EligibilityCheckResponse runCheck(String applicationCode) {
        return mockDataProvider.passed(applicationCode);
    }

    @Override
    public EligibilityCheckResponse getLatest(String applicationCode) {
        return mockDataProvider.passed(applicationCode);
    }
}
