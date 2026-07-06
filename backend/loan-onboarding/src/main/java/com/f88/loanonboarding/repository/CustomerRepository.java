package com.f88.loanonboarding.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.f88.loanonboarding.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCustomerCode(String customerCode);

    boolean existsByIdentityNumber(String identityNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Customer> findTopByCustomerCodeStartingWithOrderByCustomerCodeDesc(String prefix);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE c.identityNumber = :identityNumber
              AND c.phoneNumber = :phoneNumber
              AND LOWER(TRIM(c.fullName)) = LOWER(TRIM(:fullName))
              AND c.dateOfBirth = :dateOfBirth
            ORDER BY
                c.customerCode ASC
            """)
    List<Customer> lookupExact(
            @Param("identityNumber") String identityNumber,
            @Param("phoneNumber") String phoneNumber,
            @Param("fullName") String fullName,
            @Param("dateOfBirth") LocalDate dateOfBirth
    );

    @Query("""
            SELECT c
            FROM Customer c
            WHERE c.identityNumber = :identityNumber
               OR c.phoneNumber = :phoneNumber
            ORDER BY
                CASE
                    WHEN c.identityNumber = :identityNumber AND c.phoneNumber = :phoneNumber THEN 1
                    WHEN c.identityNumber = :identityNumber THEN 2
                    ELSE 3
                END,
                c.customerCode ASC
            """)
    List<Customer> findIdentityConflictCandidates(
            @Param("identityNumber") String identityNumber,
            @Param("phoneNumber") String phoneNumber
    );
}
