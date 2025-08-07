package com.example.autoaccountingprocessor.domain.dto;


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
@Schema(description = "회계 처리 결과")
public class ProcessingResultResponse {

    @Schema(description = "총 거래 건수", example = "9")
    private int totalTransactions;

    @Schema(description = "분류 성공 건수", example = "8")
    private int classifiedTransactions;

    @Schema(description = "분류 실패 건수", example = "1")
    private int unclassifiedTransactions;

    @Schema(description = "분류 성공률 (%)", example = "88.89")
    private double successRate;

    @Schema(description = "처리 완료 시간")
    private LocalDateTime processedAt;

    @Schema(description = "처리 소요 시간 (ms)", example = "1250")
    private long processingTimeMs;

    @Schema(description = "처리 결과 메시지", example = "처리 완료")
    private String message;

    public static ProcessingResultResponse success(int total, int classified, long processingTime) {
        int unclassified = total - classified;
        double successRate = total > 0 ? (double) classified / total * 100 : 0.0;

        return ProcessingResultResponse.builder()
                .totalTransactions(total)
                .classifiedTransactions(classified)
                .unclassifiedTransactions(unclassified)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .processedAt(LocalDateTime.now())
                .processingTimeMs(processingTime)
                .message("처리 완료")
                .build();
    }
}