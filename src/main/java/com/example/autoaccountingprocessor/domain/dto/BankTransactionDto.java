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
public class BankTransactionDto {

    private LocalDateTime transactionDate;
    private String description;
    private BigDecimal depositAmount;
    private BigDecimal withdrawalAmount;
    private BigDecimal balanceAfter;
    private String branch;
}