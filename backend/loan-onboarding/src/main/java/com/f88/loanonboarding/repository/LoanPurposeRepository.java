package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanPurpose;

public interface LoanPurposeRepository extends JpaRepository<LoanPurpose, UUID> {

    List<LoanPurpose> findByActiveTrueOrderBySortOrderAsc();

    Optional<LoanPurpose> findByCode(String code);
}
