package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.VehicleColorEntity;

public interface VehicleColorRepository extends JpaRepository<VehicleColorEntity, UUID> {
    List<VehicleColorEntity> findByActiveTrueOrderBySortOrderAscNameAsc();
}
