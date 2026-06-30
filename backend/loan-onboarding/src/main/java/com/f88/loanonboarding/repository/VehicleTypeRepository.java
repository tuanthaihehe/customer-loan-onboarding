package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleTypeEntity;

public interface VehicleTypeRepository extends JpaRepository<VehicleTypeEntity, UUID> {
    List<VehicleTypeEntity> findByActiveTrueOrderBySortOrderAscNameAsc();
}
