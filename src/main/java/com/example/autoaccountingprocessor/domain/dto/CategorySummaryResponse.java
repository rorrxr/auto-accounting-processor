package com.example.autoaccountingprocessor.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리별 요약 통계")
public class CategorySummaryResponse {

    @Schema(description = "카테고리 ID", example = "cat_101")
    private String categoryId;

    @Schema(description = "카테고리명", example = "매출")
    private String categoryName;

    @Schema(description = "총 수입 금액", example = "400000")
    private Long totalIncome;

    @Schema(description = "총 지출 금액", example = "33000")
    private Long totalExpenditure;

    @Schema(description = "순액 (수입 - 지출)", example = "367000")
    private Long netAmount;

    @Schema(description = "거래 건수", example = "5")
    private long recordCount;

    public static CategorySummaryResponse of(String categoryId, String categoryName,
                                             Long totalIncome, Long totalExpenditure,
                                             long recordCount) {
        Long income = totalIncome != null ? totalIncome : 0L;
        Long expenditure = totalExpenditure != null ? totalExpenditure : 0L;

        return CategorySummaryResponse.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .totalIncome(income)
                .totalExpenditure(expenditure)
                .netAmount(income - expenditure)
                .recordCount(recordCount)
                .build();
    }

    public static CategorySummaryResponse total(String companyName,
                                                Long totalIncome, Long totalExpenditure,
                                                long recordCount) {
        Long income = totalIncome != null ? totalIncome : 0L;
        Long expenditure = totalExpenditure != null ? totalExpenditure : 0L;

        return CategorySummaryResponse.builder()
                .categoryId("TOTAL")
                .categoryName("전체")
                .totalIncome(income)
                .totalExpenditure(expenditure)
                .netAmount(income - expenditure)
                .recordCount(recordCount)
                .build();
    }
}