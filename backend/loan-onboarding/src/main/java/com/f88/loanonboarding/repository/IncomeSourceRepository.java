package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.IncomeSource;

public interface IncomeSourceRepository extends JpaRepository<IncomeSource, UUID> {

    List<IncomeSource> findByActiveTrueOrderBySortOrderAsc();

    Optional<IncomeSource> findByCode(String code);
}
