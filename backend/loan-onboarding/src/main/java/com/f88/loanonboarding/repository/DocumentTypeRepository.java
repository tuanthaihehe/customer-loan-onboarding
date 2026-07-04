package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.DocumentType;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

    List<DocumentType> findByActiveTrueOrderBySortOrderAsc();

    Optional<DocumentType> findByCode(String code);
}
