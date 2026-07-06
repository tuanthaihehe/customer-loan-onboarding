package com.f88.loanonboarding.service;

import com.f88.loanonboarding.dto.request.creditscoring.CreditScoringCalculateRequest;
import com.f88.loanonboarding.dto.response.creditscoring.CreditScoringCalculateResponse;

public interface CreditScoringService {

    CreditScoringCalculateResponse calculate(CreditScoringCalculateRequest request);
}
