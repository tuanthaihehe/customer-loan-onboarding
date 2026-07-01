package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.Asset;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByAssetCode(String assetCode);

    Optional<Asset> findByLicensePlate(String licensePlate);

    long countByAssetCodeStartingWith(String prefix);
}
