package com.f88.loanonboarding.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.AssetValuation;

public interface AssetValuationRepository extends JpaRepository<AssetValuation, UUID> {
}
