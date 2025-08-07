package com.example.autoaccountingprocessor.domain.repository;
import com.example.autoaccountingprocessor.domain.entity.ClassifiedTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassifiedTransactionRepository extends JpaRepository<ClassifiedTransaction, Long> {

    List<ClassifiedTransaction> findByCompanyCompanyIdOrderByClassifiedAtDesc(String companyId);

    List<ClassifiedTransaction> findByCategoryCategoryIdOrderByClassifiedAtDesc(String categoryId);

    Page<ClassifiedTransaction> findByCompanyCompanyId(String companyId, Pageable pageable);

    @Query("SELECT ct FROM ClassifiedTransaction ct " +
            "JOIN FETCH ct.transaction t " +
            "WHERE ct.company.companyId = :companyId " +
            "AND t.occurredAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.occurredAt DESC")
    List<ClassifiedTransaction> findByCompanyIdAndDateRange(@Param("companyId") String companyId,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    // 통계 쿼리들 - Long 타입 사용
    @Query("SELECT COALESCE(SUM(t.deposit), 0) FROM ClassifiedTransaction ct " +
            "JOIN ct.transaction t WHERE ct.company.companyId = :companyId")
    Long getTotalDepositByCompany(@Param("companyId") String companyId);

    @Query("SELECT COALESCE(SUM(t.withdraw), 0) FROM ClassifiedTransaction ct " +
            "JOIN ct.transaction t WHERE ct.company.companyId = :companyId")
    Long getTotalWithdrawByCompany(@Param("companyId") String companyId);

    @Query("SELECT COALESCE(SUM(t.deposit), 0) FROM ClassifiedTransaction ct " +
            "JOIN ct.transaction t WHERE ct.category.categoryId = :categoryId")
    Long getTotalDepositByCategory(@Param("categoryId") String categoryId);

    @Query("SELECT COALESCE(SUM(t.withdraw), 0) FROM ClassifiedTransaction ct " +
            "JOIN ct.transaction t WHERE ct.category.categoryId = :categoryId")
    Long getTotalWithdrawByCategory(@Param("categoryId") String categoryId);

    @Query("SELECT COUNT(ct) FROM ClassifiedTransaction ct WHERE ct.company.companyId = :companyId")
    long countByCompanyId(@Param("companyId") String companyId);

    @Query("SELECT COUNT(ct) FROM ClassifiedTransaction ct WHERE ct.category.categoryId = :categoryId")
    long countByCategoryId(@Param("categoryId") String categoryId);
}
