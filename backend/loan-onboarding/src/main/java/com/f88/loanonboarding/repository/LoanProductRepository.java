package com.f88.loanonboarding.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.LoanProduct;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {
}
