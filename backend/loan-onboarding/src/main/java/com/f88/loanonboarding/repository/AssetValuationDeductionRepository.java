package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.AssetValuation;
import com.f88.loanonboarding.entity.AssetValuationDeduction;

public interface AssetValuationDeductionRepository extends JpaRepository<AssetValuationDeduction, UUID> {

    List<AssetValuationDeduction> findByAssetValuationOrderByCreatedAtAsc(AssetValuation assetValuation);
}
