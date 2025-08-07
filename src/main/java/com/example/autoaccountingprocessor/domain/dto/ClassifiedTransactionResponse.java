package com.example.autoaccountingprocessor.domain.dto;


import com.example.autoaccountingprocessor.domain.entity.ClassifiedTransaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "분류 성공한 거래 응답")
public class ClassifiedTransactionResponse {

    @Schema(description = "분류 거래 ID", example = "1")
    private Long classifiedTxId;

    @Schema(description = "원본 거래 ID", example = "101")
    private Long txId;

    @Schema(description = "거래 발생 일시", example = "2025-07-20T13:45:11")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurredAt;

    @Schema(description = "거래 설명", example = "스타벅스 강남2호점")
    private String description;

    @Schema(description = "입금액", example = "0")
    private Long deposit;

    @Schema(description = "출금액", example = "5500")
    private Long withdraw;

    @Schema(description = "거래 후 잔액", example = "994500")
    private Long balance;

    @Schema(description = "거래 지점", example = "강남지점")
    private String branchInfo;

    @Schema(description = "회사 ID", example = "com_2")
    private String companyId;

    @Schema(description = "회사명", example = "B 커머스")
    private String companyName;

    @Schema(description = "카테고리 ID", example = "cat_204")
    private String categoryId;

    @Schema(description = "카테고리명", example = "복리후생비")
    private String categoryName;

    @Schema(description = "매칭된 키워드", example = "스타벅스")
    private String matchedKeyword;

    @Schema(description = "분류 처리 시간", example = "2025-07-20T13:46:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime classifiedAt;

    public static ClassifiedTransactionResponse from(ClassifiedTransaction ct) {
        return ClassifiedTransactionResponse.builder()
                .classifiedTxId(ct.getClassifiedTxId())
                .txId(ct.getTransaction().getId())
                .occurredAt(ct.getOccurredAt())
                .description(ct.getDescription())
                .deposit(ct.getDeposit())
                .withdraw(ct.getWithdraw())
                .balance(ct.getTransaction().getBalance())
                .branchInfo(ct.getTransaction().getBranchInfo())
                .companyId(ct.getCompany().getCompanyId())
                .companyName(ct.getCompanyName())
                .categoryId(ct.getCategory().getCategoryId())
                .categoryName(ct.getCategoryName())
                .matchedKeyword(ct.getMatchedKeyword())
                .classifiedAt(ct.getClassifiedAt())
                .build();
    }
}