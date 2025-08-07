package com.example.autoaccountingprocessor.domain.repository;

import com.example.autoaccountingprocessor.domain.entity.UnclassifiedTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
public interface UnclassifiedTransactionRepository extends JpaRepository<UnclassifiedTransaction, Long> {

    List<UnclassifiedTransaction> findByCompanyCompanyIdOrderById(String companyId);

    List<UnclassifiedTransaction> findByCompanyCompanyIdAndReviewedOrderById(String companyId, boolean reviewed);

    Page<UnclassifiedTransaction> findByCompanyCompanyId(String companyId, Pageable pageable);

    @Query("SELECT ut FROM UnclassifiedTransaction ut " +
            "JOIN FETCH ut.transaction t " +
            "WHERE ut.company.companyId = :companyId " +
            "AND t.occurredAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.occurredAt DESC")
    List<UnclassifiedTransaction> findByCompanyIdAndDateRange(@Param("companyId") String companyId,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(ut) FROM UnclassifiedTransaction ut WHERE ut.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT COUNT(ut) FROM UnclassifiedTransaction ut " +
            "WHERE ut.company.companyId = :companyId AND ut.reviewed = false")
    long countUnreviewedByCompanyId(@Param("companyId") String companyId);
}
