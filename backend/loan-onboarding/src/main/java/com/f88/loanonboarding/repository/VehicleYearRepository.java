package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.f88.loanonboarding.entity.VehicleYearEntity;

public interface VehicleYearRepository extends JpaRepository<VehicleYearEntity, UUID> {

    @Query("""
            SELECT DISTINCT y.manufactureYear
            FROM VehicleYearEntity y
            WHERE y.active = true
            ORDER BY y.manufactureYear DESC
            """)
    List<Integer> findActiveManufactureYears();
}
