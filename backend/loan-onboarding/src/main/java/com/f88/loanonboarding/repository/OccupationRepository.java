package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.Occupation;

public interface OccupationRepository extends JpaRepository<Occupation, UUID> {

    List<Occupation> findByActiveTrueOrderBySortOrderAsc();

    Optional<Occupation> findByCode(String code);
}
