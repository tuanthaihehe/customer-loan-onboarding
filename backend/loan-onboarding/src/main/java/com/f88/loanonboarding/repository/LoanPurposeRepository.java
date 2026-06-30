package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanPurposeEntity;

public interface LoanPurposeRepository extends JpaRepository<LoanPurposeEntity, UUID> {

    Optional<LoanPurposeEntity> findByCodeAndActiveTrue(String code);

    List<LoanPurposeEntity> findByActiveTrueOrderBySortOrderAscNameAsc();
}
