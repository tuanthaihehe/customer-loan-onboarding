package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationEntity;

public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, UUID> {

    boolean existsByLoanApplicationCode(String loanApplicationCode);

    @EntityGraph(attributePaths = {"customer", "currentState", "loanPurpose", "loanTerm", "asset"})
    Optional<LoanApplicationEntity> findByLoanApplicationCode(String loanApplicationCode);

    Optional<LoanApplicationEntity> findTopByLoanApplicationCodeStartingWithOrderByLoanApplicationCodeDesc(String prefix);
}
