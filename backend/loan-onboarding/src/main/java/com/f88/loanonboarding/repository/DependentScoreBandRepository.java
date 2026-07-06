package com.f88.loanonboarding.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.DependentScoreBand;

public interface DependentScoreBandRepository extends JpaRepository<DependentScoreBand, UUID> {

    @Query(value = """
            SELECT *
            FROM dependent_score_band band
            WHERE band.rule_set_code = :ruleSetCode
              AND band.is_active = TRUE
              AND band.effective_from <= :asOfDate
              AND (band.effective_to IS NULL OR band.effective_to >= :asOfDate)
              AND :dependentCount >= band.min_dependent_count
              AND (band.max_dependent_count IS NULL OR :dependentCount < band.max_dependent_count)
            ORDER BY band.priority ASC
            limit 1
            """, nativeQuery = true)
    Optional<DependentScoreBand> findActiveBand(
            @Param("ruleSetCode") String ruleSetCode,
            @Param("dependentCount") int dependentCount,
            @Param("asOfDate") LocalDate asOfDate
    );
}
