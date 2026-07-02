package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.Bank;

public interface BankRepository extends JpaRepository<Bank, UUID> {

    List<Bank> findByActiveTrueOrderBySortOrderAsc();

    Optional<Bank> findByCode(String code);
}
