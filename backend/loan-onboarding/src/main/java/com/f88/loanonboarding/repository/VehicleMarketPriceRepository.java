package com.f88.loanonboarding.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.VehicleMarketPrice;
import com.f88.loanonboarding.entity.VehicleVariant;

public interface VehicleMarketPriceRepository extends JpaRepository<VehicleMarketPrice, UUID> {

    @Query("""
            select price
            from VehicleMarketPrice price
            where price.vehicleVariant = :vehicleVariant
              and price.effectiveFrom <= :businessDate
              and (price.effectiveTo is null or price.effectiveTo >= :businessDate)
            order by price.effectiveFrom desc
            """)
    List<VehicleMarketPrice> findEffectivePrices(
            @Param("vehicleVariant") VehicleVariant vehicleVariant,
            @Param("businessDate") LocalDate businessDate
    );
}
