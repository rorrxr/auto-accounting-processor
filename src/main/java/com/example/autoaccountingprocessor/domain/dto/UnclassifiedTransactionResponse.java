package com.example.autoaccountingprocessor.domain.dto;


import com.example.autoaccountingprocessor.domain.entity.UnclassifiedTransaction;
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
@Schema(description = "미분류 거래 응답")
public class UnclassifiedTransactionResponse {

    @Schema(description = "미분류 거래 ID", example = "1")
    private Long id;

    @Schema(description = "원본 거래 ID", example = "107")
    private Long txId;

    @Schema(description = "거래 발생 일시", example = "2025-07-21T21:00:15")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurredAt;

    @Schema(description = "거래 설명", example = "개인용도 이체")
    private String description;

    @Schema(description = "입금액", example = "0")
    private Long deposit;

    @Schema(description = "출금액", example = "100000")
    private Long withdraw;

    @Schema(description = "거래 후 잔액", example = "1249000")
    private Long balance;

    @Schema(description = "거래 지점", example = "강남지점")
    private String branchInfo;

    @Schema(description = "회사 ID", example = "com_1")
    private String companyId;

    @Schema(description = "회사명", example = "A 커머스")
    private String companyName;

    @Schema(description = "분류 실패 사유", example = "키워드 미일치")
    private String reason;

    @Schema(description = "검토 완료 여부", example = "false")
    private boolean reviewed;

    public static UnclassifiedTransactionResponse from(UnclassifiedTransaction ut) {
        return UnclassifiedTransactionResponse.builder()
                .id(ut.getId())
                .txId(ut.getTransaction().getId())
                .occurredAt(ut.getOccurredAt())
                .description(ut.getDescription())
                .deposit(ut.getDeposit())
                .withdraw(ut.getWithdraw())
                .balance(ut.getTransaction().getBalance())
                .branchInfo(ut.getTransaction().getBranchInfo())
                .companyId(ut.getCompany().getCompanyId())
                .companyName(ut.getCompany().getCompanyName())
                .reason(ut.getReason())
                .reviewed(ut.isReviewed())
                .build();
    }
}
