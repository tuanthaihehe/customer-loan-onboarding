package com.f88.loanonboarding.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.f88.loanonboarding.entity.Asset;
import com.f88.loanonboarding.entity.LoanApplication;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    @EntityGraph(attributePaths = {
            "customer",
            "currentState",
            "currentStep",
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

    boolean existsByLoanApplicationCode(String loanApplicationCode);

    @EntityGraph(attributePaths = {"customer", "currentState", "currentStep"})
    List<LoanApplication> findByCurrentState_CodeOrderByUpdatedAtDesc(String stateCode);

    @EntityGraph(attributePaths = {"customer", "currentState", "loanPurpose", "loanProduct"})
    List<LoanApplication> findByCurrentState_CodeInOrderByUpdatedAtDesc(List<String> stateCodes);

    @EntityGraph(attributePaths = {"customer", "currentState", "currentStep"})
    List<LoanApplication> findAllByOrderByUpdatedAtDesc();

    boolean existsByAssetAndCurrentState_TerminalFalseAndLoanApplicationCodeNot(
            Asset asset,
            String loanApplicationCode
    );
}
