package com.example.autoaccountingprocessor.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationResult {

    private Long transactionId;
    private LocalDateTime transactionDate;
    private String description;
    private BigDecimal amount;
    private String transactionType; // DEPOSIT, WITHDRAWAL
    private String companyId;
    private String companyName;
    private String categoryId;
    private String categoryName;
    private String matchedKeyword;
    private boolean isClassified;
    private String failureReason; // 분류 실패 시 사유
}