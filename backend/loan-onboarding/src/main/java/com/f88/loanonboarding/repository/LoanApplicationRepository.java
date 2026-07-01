package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.LoanApplication;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    boolean existsByLoanApplicationCode(String loanApplicationCode);

    @EntityGraph(attributePaths = {
            "customer",
            "currentState",
            "loanPurpose",
            "loanTerm",
            "asset",
            "asset.vehicleVariant",
            "asset.vehicleVariant.vehicleColor",
            "asset.vehicleVariant.vehicleYear",
            "asset.vehicleVariant.vehicleYear.vehicleVersion",
            "asset.vehicleVariant.vehicleYear.vehicleVersion.vehicleModel",
            "asset.vehicleVariant.vehicleYear.vehicleVersion.vehicleModel.vehicleBrand",
            "asset.vehicleVariant.vehicleYear.vehicleVersion.vehicleModel.vehicleBrand.vehicleType"
    })
    Optional<LoanApplication> findByLoanApplicationCode(String loanApplicationCode);

    Optional<LoanApplication> findTopByLoanApplicationCodeStartingWithOrderByLoanApplicationCodeDesc(String prefix);

    boolean existsByAssetAndCurrentState_TerminalFalseAndLoanApplicationCodeNot(
            Asset asset,
            String loanApplicationCode
    );
}
