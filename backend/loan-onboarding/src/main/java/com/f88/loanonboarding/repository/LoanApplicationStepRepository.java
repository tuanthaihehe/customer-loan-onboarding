package com.f88.loanonboarding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationStep;

public interface LoanApplicationStepRepository extends JpaRepository<LoanApplicationStep, String> {

    List<LoanApplicationStep> findByActiveTrueOrderByStepOrderAsc();
}
