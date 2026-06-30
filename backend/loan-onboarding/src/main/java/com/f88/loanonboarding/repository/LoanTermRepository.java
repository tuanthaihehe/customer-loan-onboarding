package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanTermEntity;

public interface LoanTermRepository extends JpaRepository<LoanTermEntity, UUID> {

    Optional<LoanTermEntity> findByTermMonthsAndActiveTrue(Integer termMonths);
}
