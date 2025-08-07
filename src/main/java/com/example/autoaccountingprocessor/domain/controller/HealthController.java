package com.example.autoaccountingprocessor.domain.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "시스템 관리", description = "시스템 상태 및 정보 확인 API")
public class HealthController {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Operation(
            summary = "헬스체크",
            description = "애플리케이션 상태를 확인합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "정상 상태",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "헬스체크 응답",
                            value = """
                {
                  "status": "UP",
                  "timestamp": "2025-08-07T16:30:00",
                  "version": "1.0.0"
                }
                """
                    )
            )
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "version", appVersion
        ));
    }

    @Operation(
            summary = "애플리케이션 정보",
            description = "애플리케이션의 상세 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "애플리케이션 정보",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "애플리케이션 정보 응답",
                            value = """
                {
                  "name": "Auto Accounting Processor",
                  "version": "1.0.0",
                  "description": "은행 거래 내역 자동 분류 시스템",
                  "features": {
                    "csvProcessing": "CSV 파일 파싱 및 거래 내역 저장",
                    "ruleBasedClassification": "키워드 기반 자동 분류",
                    "summaryReports": "카테고리별 수입/지출 요약",
                    "unclassifiedHandling": "미분류 거래 관리"
                  }
                }
                """
                    )
            )
    )
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
                "name", "Auto Accounting Processor",
                "version", appVersion,
                "description", "은행 거래 내역 자동 분류 시스템",
                "features", Map.of(
                        "csvProcessing", "CSV 파일 파싱 및 거래 내역 저장",
                        "ruleBasedClassification", "키워드 기반 자동 분류",
                        "summaryReports", "카테고리별 수입/지출 요약",
                        "unclassifiedHandling", "미분류 거래 관리"
                )
        ));
    }
}