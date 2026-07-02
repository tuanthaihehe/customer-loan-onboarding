package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.MockScoreGradeRule;

public interface MockScoreGradeRuleRepository extends JpaRepository<MockScoreGradeRule, UUID> {

    @EntityGraph(attributePaths = "scoreGrade")
    List<MockScoreGradeRule> findByActiveTrueOrderBySortOrderAsc();
}
