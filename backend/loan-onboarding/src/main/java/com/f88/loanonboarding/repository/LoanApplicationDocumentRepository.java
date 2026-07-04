package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationDocument;

public interface LoanApplicationDocumentRepository extends JpaRepository<LoanApplicationDocument, UUID> {

    @EntityGraph(attributePaths = "documentType")
    List<LoanApplicationDocument> findByLoanApplicationId(UUID loanApplicationId);
}
