package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.response.loan.EligibilityCheckResponse;
import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.enums.EligibilityResult;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.repository.LoanApplicationRepository;
import com.f88.loanonboarding.service.EligibilityService;

@Service
public class EligibilityServiceImpl implements EligibilityService {

    private final LoanApplicationRepository loanApplicationRepository;

    public EligibilityServiceImpl(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityCheckResponse runCheck(String applicationCode) {
        return evaluate(applicationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityCheckResponse getLatest(String applicationCode) {
        return evaluate(applicationCode);
    }

    private EligibilityCheckResponse evaluate(String applicationCode) {
        LoanApplication application = loanApplicationRepository.findByLoanApplicationCode(applicationCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND));

        int total = 3;
        int completed = 0;
        completed += application.getRequestedAmount() == null ? 0 : 1;
        completed += application.getLoanPurpose() == null ? 0 : 1;
        completed += application.getLoanTermMonths() == null ? 0 : 1;
        int missing = total - completed;

        return new EligibilityCheckResponse(
                "ELG-" + applicationCode,
                applicationCode,
                missing == 0 ? "COMPLETED" : "INCOMPLETE",
                missing == 0 ? EligibilityResult.PASSED : EligibilityResult.FAILED,
                total,
                completed,
                missing,
                0,
                LocalDateTime.now()
        );
    }
}
