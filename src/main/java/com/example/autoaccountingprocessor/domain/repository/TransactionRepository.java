package com.example.autoaccountingprocessor.domain.repository;

import com.example.autoaccountingprocessor.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompanyId(String companyId);
}