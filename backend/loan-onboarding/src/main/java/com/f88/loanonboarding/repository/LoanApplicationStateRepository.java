package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanApplicationStateEntity;

public interface LoanApplicationStateRepository extends JpaRepository<LoanApplicationStateEntity, UUID> {

    Optional<LoanApplicationStateEntity> findByCode(String code);
}
