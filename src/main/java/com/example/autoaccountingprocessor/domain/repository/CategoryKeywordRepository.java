package com.example.autoaccountingprocessor.domain.repository;

import com.example.autoaccountingprocessor.domain.entity.CategoryKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryKeywordRepository extends JpaRepository<CategoryKeyword, Long> {

    List<CategoryKeyword> findByCompanyCompanyIdOrderByCreatedAtDesc(String companyId);

    List<CategoryKeyword> findByCategoryCategoryIdOrderByCreatedAtDesc(String categoryId);

    @Query("SELECT ck FROM CategoryKeyword ck " +
            "JOIN FETCH ck.company c " +
            "JOIN FETCH ck.category cat " +
            "WHERE c.companyId = :companyId")
    List<CategoryKeyword> findByCompanyIdWithDetails(@Param("companyId") String companyId);

    @Query("SELECT ck FROM CategoryKeyword ck " +
            "JOIN FETCH ck.company " +
            "JOIN FETCH ck.category " +
            "ORDER BY ck.createdAt DESC")
    List<CategoryKeyword> findAllWithDetails();

    Optional<CategoryKeyword> findByCompanyCompanyIdAndCategoryCategoryIdAndKeyword(
            String companyId, String categoryId, String keyword);
}
