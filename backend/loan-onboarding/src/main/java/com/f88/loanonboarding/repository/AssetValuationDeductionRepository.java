package com.f88.loanonboarding.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.AssetValuationDeduction;

public interface AssetValuationDeductionRepository extends JpaRepository<AssetValuationDeduction, UUID> {
}
