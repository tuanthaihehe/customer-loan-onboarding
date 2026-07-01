package com.f88.loanonboarding.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.LoanProduct;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {

    @EntityGraph(attributePaths = {
            "loanPurposes",
            "vehicleTypes",
            "loanTerms",
            "scoreGrades"
    })
    @Query("""
            select distinct product
            from LoanProduct product
            join product.vehicleTypes vehicleType
            join product.loanTerms loanTerm
            join product.scoreGrades scoreGrade
            left join product.loanPurposes loanPurpose
            where product.active = true
              and vehicleType.active = true
              and vehicleType.code = :vehicleTypeCode
              and loanTerm.active = true
              and loanTerm.termMonths = :loanTermMonths
              and scoreGrade.active = true
              and scoreGrade.code = :scoreGradeCode
              and (
                    product.appliesToAllLoanPurposes = true
                    or loanPurpose.code = :loanPurposeCode
              )
            order by product.sortOrder asc
            """)
    List<LoanProduct> findMatchingProducts(
            @Param("loanPurposeCode") String loanPurposeCode,
            @Param("vehicleTypeCode") String vehicleTypeCode,
            @Param("loanTermMonths") Integer loanTermMonths,
            @Param("scoreGradeCode") String scoreGradeCode
    );
}
