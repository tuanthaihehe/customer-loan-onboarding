package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationReferencePerson;

public interface LoanApplicationReferencePersonRepository
        extends JpaRepository<LoanApplicationReferencePerson, UUID> {

    List<LoanApplicationReferencePerson> findByLoanApplicationId(UUID loanApplicationId);

    void deleteByLoanApplicationId(UUID loanApplicationId);
}
