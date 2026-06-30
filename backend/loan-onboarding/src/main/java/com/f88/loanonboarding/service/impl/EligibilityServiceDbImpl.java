package com.f88.loanonboarding.service.impl;

import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.f88.loanonboarding.common.error.ErrorCode;
import com.f88.loanonboarding.dto.response.loan.EligibilityCheckResponse;
import com.f88.loanonboarding.enums.EligibilityResult;
import com.f88.loanonboarding.exception.BusinessException;
import com.f88.loanonboarding.service.EligibilityService;

@Service
public class EligibilityServiceDbImpl implements EligibilityService {

    private final JdbcTemplate jdbcTemplate;

    public EligibilityServiceDbImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public EligibilityCheckResponse runCheck(String applicationCode) {
        ensureApplicationExists(applicationCode);
        return response(applicationCode);
    }

    @Override
    public EligibilityCheckResponse getLatest(String applicationCode) {
        ensureApplicationExists(applicationCode);
        return response(applicationCode);
    }

    private void ensureApplicationExists(String applicationCode) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM loan_application WHERE loan_application_code = ?",
                Integer.class,
                applicationCode
        );
        if (count == null || count == 0) {
            throw new BusinessException(ErrorCode.LOAN_APPLICATION_NOT_FOUND, "Không tìm thấy hồ sơ vay trong database");
        }
    }

    private EligibilityCheckResponse response(String applicationCode) {
        return new EligibilityCheckResponse(
                null,
                applicationCode,
                "NOT_PERSISTED",
                EligibilityResult.PASSED,
                0,
                0,
                0,
                0,
                LocalDateTime.now()
        );
    }
}
