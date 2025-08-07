package com.example.autoaccountingprocessor.domain.repository;


import com.example.autoaccountingprocessor.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
}