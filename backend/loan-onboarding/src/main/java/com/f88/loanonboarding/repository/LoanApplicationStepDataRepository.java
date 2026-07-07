package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplication;
import com.f88.loanonboarding.entity.LoanApplicationStepData;

public interface LoanApplicationStepDataRepository extends JpaRepository<LoanApplicationStepData, UUID> {

    List<LoanApplicationStepData> findByLoanApplication_LoanApplicationCodeOrderByStep_StepOrderAsc(String applicationCode);

    Optional<LoanApplicationStepData> findByLoanApplication_LoanApplicationCodeAndStep_Code(String applicationCode, String stepCode);

    List<LoanApplicationStepData> findByLoanApplicationAndStep_StepOrderGreaterThan(
            LoanApplication loanApplication,
            int stepOrder
    );
}
