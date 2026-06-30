package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.AssetDeductionTypeEntity;

public interface AssetDeductionTypeRepository extends JpaRepository<AssetDeductionTypeEntity, UUID> {
    List<AssetDeductionTypeEntity> findByActiveTrueOrderBySortOrderAscNameAsc();
}
