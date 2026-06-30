package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplication;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    @EntityGraph(attributePaths = {"customer", "currentState"})
    Optional<LoanApplication> findByLoanApplicationCode(String loanApplicationCode);

    boolean existsByLoanApplicationCode(String loanApplicationCode);
}
