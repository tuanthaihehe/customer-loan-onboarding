package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanProduct;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {

    @EntityGraph(attributePaths = {"loanPurposes", "vehicleTypes", "loanTerms", "scoreGrades"})
    List<LoanProduct> findByActiveTrueOrderBySortOrderAsc();

    @EntityGraph(attributePaths = {"loanPurposes", "vehicleTypes", "loanTerms", "scoreGrades"})
    Optional<LoanProduct> findByProductCode(String productCode);
}
