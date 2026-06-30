package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.AssetDeductionType;

public interface AssetDeductionTypeRepository extends JpaRepository<AssetDeductionType, UUID> {

    List<AssetDeductionType> findByActiveTrueOrderBySortOrderAsc();

    Optional<AssetDeductionType> findByCode(String code);
}
