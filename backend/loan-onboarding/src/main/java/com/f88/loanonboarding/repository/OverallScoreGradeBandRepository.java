package com.f88.loanonboarding.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.OverallScoreGradeBand;

public interface OverallScoreGradeBandRepository extends JpaRepository<OverallScoreGradeBand, UUID> {

    @Query(value = """
            SELECT *
            FROM overall_score_grade_band band
            WHERE band.rule_set_code = :ruleSetCode
              AND band.is_active = TRUE
              AND band.effective_from <= :asOfDate
              AND (band.effective_to IS NULL OR band.effective_to >= :asOfDate)
              AND :score >= band.min_score
              AND (band.max_score IS NULL OR :score < band.max_score)
            ORDER BY band.priority ASC
            limit 1
            """, nativeQuery = true)
    Optional<OverallScoreGradeBand> findActiveBand(
            @Param("ruleSetCode") String ruleSetCode,
            @Param("score") BigDecimal score,
            @Param("asOfDate") LocalDate asOfDate
    );
}
