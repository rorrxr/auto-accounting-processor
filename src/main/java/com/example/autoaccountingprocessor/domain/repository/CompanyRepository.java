package com.example.autoaccountingprocessor.domain.repository;


import com.example.autoaccountingprocessor.domain.entity.Category;
import com.example.autoaccountingprocessor.domain.entity.Company;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    List<Company> findAllByOrderByCreatedAtDesc();

    Optional<Company> findByCompanyName(String companyName);

    boolean existsByCompanyId(String companyId);
}