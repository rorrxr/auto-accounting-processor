package com.example.autoaccountingprocessor.domain.dto;

import com.example.autoaccountingprocessor.domain.entity.Transaction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AccountingRecordResponseDto {
    private Long transactionId;
    private LocalDate date;
    private String description;
    private Integer amount;
    private Long categoryId;
    private String categoryName;

    public static AccountingRecordResponseDto from(Transaction tx) {
        return AccountingRecordResponseDto.builder()
                .transactionId(tx.getId())
                .date(tx.getDate())
                .description(tx.getDescription())
                .amount(tx.getAmount())
                .categoryId(tx.getCategory() != null ? tx.getCategory().getId() : null)
                .categoryName(tx.getCategory() != null ? tx.getCategory().getName() : "미분류")
                .build();
    }
}