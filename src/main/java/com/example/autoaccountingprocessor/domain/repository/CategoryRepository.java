package com.example.autoaccountingprocessor.domain.repository;

import com.example.autoaccountingprocessor.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    List<Category> findByCompanyCompanyIdOrderByCreatedAtDesc(String companyId);

    Optional<Category> findByCategoryIdAndCompanyCompanyId(String categoryId, String companyId);

    boolean existsByCategoryIdAndCompanyCompanyId(String categoryId, String companyId);
}