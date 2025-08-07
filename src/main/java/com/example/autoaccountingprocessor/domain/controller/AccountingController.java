package com.example.autoaccountingprocessor.domain.controller;

import com.example.autoaccountingprocessor.common.dto.ApiResponse;
import com.example.autoaccountingprocessor.domain.dto.AccountingRecordResponseDto;
import com.example.autoaccountingprocessor.domain.service.AccountingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounting")
@RequiredArgsConstructor
@Tag(name = "Accounting", description = "자동 회계 처리 API")
public class AccountingController {

    private final AccountingService accountingService;

    @PostMapping("/process")
    @Operation(summary = "CSV + 규칙 파일 업로드", description = "CSV와 rules.json을 업로드하여 자동 회계 분류 처리")
    public ResponseEntity<ApiResponse<Void>> processTransactions(
            @RequestParam("csvFile") MultipartFile csvFile,
            @RequestParam("rulesFile") MultipartFile rulesFile) {
        accountingService.process(csvFile, rulesFile);
        return ResponseEntity.ok(ApiResponse.success(null, "자동 분류 처리 완료"));
    }

    @GetMapping("/records")
    @Operation(summary = "사업체별 거래 조회", description = "companyId로 해당 거래 목록 및 분류 정보 조회")
    public ResponseEntity<ApiResponse<List<AccountingRecordResponseDto>>> getRecords(
            @RequestParam("companyId") String companyId) {
        List<AccountingRecordResponseDto> records = accountingService.getRecordsByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
