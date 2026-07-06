package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.KycProfile;

public interface KycProfileRepository extends JpaRepository<KycProfile, UUID> {

    List<KycProfile> findByCustomer_CustomerCodeOrderByCheckedAtDesc(String customerCode);

    Optional<KycProfile> findByLoanApplication_LoanApplicationCode(String loanApplicationCode);
}
