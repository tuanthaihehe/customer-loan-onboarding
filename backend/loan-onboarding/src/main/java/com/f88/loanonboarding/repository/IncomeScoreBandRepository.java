package com.f88.loanonboarding.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.IncomeScoreBand;

public interface IncomeScoreBandRepository extends JpaRepository<IncomeScoreBand, UUID> {

    @Query(value = """
            SELECT *
            FROM income_score_band band
            WHERE band.rule_set_code = :ruleSetCode
              AND band.is_active = TRUE
              AND band.effective_from <= :asOfDate
              AND (band.effective_to IS NULL OR band.effective_to >= :asOfDate)
              AND :incomeAmount >= band.min_income_amount
              AND (band.max_income_amount IS NULL OR :incomeAmount < band.max_income_amount)
            ORDER BY band.priority ASC
            limit 1
            """, nativeQuery = true)
    Optional<IncomeScoreBand> findActiveBand(
            @Param("ruleSetCode") String ruleSetCode,
            @Param("incomeAmount") BigDecimal incomeAmount,
            @Param("asOfDate") LocalDate asOfDate
    );
}
