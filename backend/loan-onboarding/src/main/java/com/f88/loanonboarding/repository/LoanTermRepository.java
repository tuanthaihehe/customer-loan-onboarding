package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanTerm;

public interface LoanTermRepository extends JpaRepository<LoanTerm, UUID> {

    Optional<LoanTerm> findByTermMonthsAndActiveTrue(Integer termMonths);
}
