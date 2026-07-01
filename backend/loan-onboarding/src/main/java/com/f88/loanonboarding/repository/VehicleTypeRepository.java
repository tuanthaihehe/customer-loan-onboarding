package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleType;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, UUID> {

    List<VehicleType> findByActiveTrueOrderBySortOrderAsc();

    List<VehicleType> findByActiveTrueOrderBySortOrderAscNameAsc();
}
