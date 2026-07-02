package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.AssetValuation;

public interface AssetValuationRepository extends JpaRepository<AssetValuation, UUID> {

    Optional<AssetValuation> findTopByAssetOrderByValuedAtDesc(Asset asset);
}
