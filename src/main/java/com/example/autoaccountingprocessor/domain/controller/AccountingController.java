package com.example.autoaccountingprocessor.domain.controller;

import com.example.autoaccountingprocessor.common.dto.ApiResponse;
import com.example.autoaccountingprocessor.domain.dto.*;
import com.example.autoaccountingprocessor.domain.service.AccountingService;
import com.example.autoaccountingprocessor.domain.service.CsvParsingService;
import com.example.autoaccountingprocessor.domain.service.RuleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


import com.example.autoaccountingprocessor.domain.entity.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    @Operation(
            summary = "거래 내역 업로드 및 자동 분류",
            description = "CSV 형식의 은행 거래 내역과 JSON 형식의 분류 규칙을 업로드하여 자동으로 거래를 분류합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "거래 내역 CSV 파일과 분류 규칙 JSON 파일",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProcessingResultResponse>> processAccounting(
            @Parameter(description = "은행 거래 내역 CSV 파일", required = true)
            @RequestParam("transactions") MultipartFile transactionsFile,

            @Parameter(description = "분류 규칙 JSON 파일", required = true)
            @RequestParam("rules") MultipartFile rulesFile) {

        try {
            validateFiles(transactionsFile, rulesFile);

            ProcessingResultResponse result = accountingService.processAccountingFiles(
                    transactionsFile, rulesFile);

            return ResponseEntity.ok(ApiResponse.success("처리 완료", result));

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "INVALID_REQUEST"));

        } catch (Exception e) {
            log.error("회계 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("회계 처리 중 오류가 발생했습니다.", "PROCESSING_ERROR"));
        }
    }

    @Operation(
            summary = "분류된 거래 조회",
            description = "특정 회사의 분류 성공한 거래 내역을 조회합니다."
    )
    @GetMapping("/records")
    public ResponseEntity<List<ClassifiedTransactionResponse>> getClassifiedRecords(
            @Parameter(description = "회사 ID", example = "com_1", required = true)
            @RequestParam String companyId) {

        try {
            List<ClassifiedTransactionResponse> records =
                    accountingService.getClassifiedRecords(companyId);
            return ResponseEntity.ok(records);

        } catch (Exception e) {
            log.error("분류된 거래 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "분류된 거래 조회 (페이징)",
            description = "특정 회사의 분류 성공한 거래 내역을 페이징하여 조회합니다."
    )
    @GetMapping("/records/paged")
    public ResponseEntity<Page<ClassifiedTransactionResponse>> getClassifiedRecordsPaged(
            @Parameter(description = "회사 ID", example = "com_1", required = true)
            @RequestParam String companyId,

            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 20) Pageable pageable) {

        try {
            Page<ClassifiedTransactionResponse> records =
                    accountingService.getClassifiedRecords(companyId, pageable);
            return ResponseEntity.ok(records);

        } catch (Exception e) {
            log.error("분류된 거래 페이징 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "미분류 거래 조회",
            description = "특정 회사의 분류에 실패한 거래 내역을 조회합니다."
    )
    @GetMapping("/unclassified")
    public ResponseEntity<List<UnclassifiedTransactionResponse>> getUnclassifiedRecords(
            @Parameter(description = "회사 ID", example = "com_1", required = true)
            @RequestParam String companyId) {

        try {
            List<UnclassifiedTransactionResponse> records =
                    accountingService.getUnclassifiedRecords(companyId);
            return ResponseEntity.ok(records);

        } catch (Exception e) {
            log.error("미분류 거래 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "전체 수입/지출 요약",
            description = "특정 회사의 전체 수입/지출을 요약하여 조회합니다."
    )
    @GetMapping("/summary/total/{companyId}")
    public ResponseEntity<CategorySummaryResponse> getTotalSummary(
            @Parameter(description = "회사 ID", example = "com_1", required = true)
            @PathVariable String companyId) {

        try {
            CategorySummaryResponse summary = accountingService.getTotalSummary(companyId);
            return ResponseEntity.ok(summary);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 회사 ID: {}", companyId);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("전체 요약 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "카테고리별 수입/지출 요약",
            description = "특정 회사의 카테고리별 수입/지출을 요약하여 조회합니다."
    )
    @GetMapping("/summary/categories/{companyId}")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummaries(
            @Parameter(description = "회사 ID", example = "com_1", required = true)
            @PathVariable String companyId) {

        try {
            List<CategorySummaryResponse> summaries =
                    accountingService.getCategorySummaries(companyId);
            return ResponseEntity.ok(summaries);

        } catch (Exception e) {
            log.error("카테고리별 요약 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFiles(MultipartFile transactionsFile, MultipartFile rulesFile) {
        if (transactionsFile.isEmpty()) {
            throw new IllegalArgumentException("거래 내역 파일이 비어있습니다.");
        }

        if (rulesFile.isEmpty()) {
            throw new IllegalArgumentException("분류 규칙 파일이 비어있습니다.");
        }

        if (!transactionsFile.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("거래 내역은 CSV 파일이어야 합니다.");
        }

        if (!rulesFile.getOriginalFilename().toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("분류 규칙은 JSON 파일이어야 합니다.");
        }

        // 파일 크기 검증 (50MB 제한)
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (transactionsFile.getSize() > maxSize || rulesFile.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 50MB를 초과할 수 없습니다.");
        }
    }
}

