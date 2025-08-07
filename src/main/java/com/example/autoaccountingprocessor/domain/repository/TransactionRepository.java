package com.example.autoaccountingprocessor.domain.repository;

import com.example.autoaccountingprocessor.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    List<Transaction> findByCompanyCompanyIdOrderByOccurredAtDesc(String companyId);

    Page<Transaction> findByCompanyCompanyId(String companyId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.company.companyId = :companyId " +
            "AND t.occurredAt BETWEEN :startDate AND :endDate ORDER BY t.occurredAt DESC")
    List<Transaction> findByCompanyIdAndDateRange(@Param("companyId") String companyId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") String companyId);
}