package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationState;

public interface LoanApplicationStateRepository extends JpaRepository<LoanApplicationState, UUID> {

    Optional<LoanApplicationState> findByCode(String code);

    Optional<LoanApplicationState> findByInitialTrue();

    List<LoanApplicationState> findAllByOrderBySortOrderAsc();
}
