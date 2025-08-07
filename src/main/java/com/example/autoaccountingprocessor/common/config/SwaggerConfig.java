package com.example.autoaccountingprocessor.common.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버"),
                        new Server()
                                .url("https://api.accounting.example.com")
                                .description("운영 서버")
                ))
                .tags(List.of(
                        new Tag()
                                .name("자동 회계 처리")
                                .description("은행 거래 내역 자동 분류 및 조회 API"),
                        new Tag()
                                .name("시스템 관리")
                                .description("시스템 상태 및 정보 확인 API")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("자동 회계 처리 시스템 API")
                .description("""
                    ## 개요
                    은행 거래 내역 CSV 파일을 업로드하여 미리 정의된 분류 규칙에 따라 자동으로 회계 카테고리를 분류하는 시스템입니다.
                    
                    ## 주요 기능
                    - **CSV 파일 처리**: 은행 거래 내역 자동 파싱 및 저장
                    - **규칙 기반 분류**: JSON 키워드 규칙을 통한 자동 분류
                    - **분리된 결과 관리**: 분류 성공/실패 거래 별도 저장
                    - **통계 및 요약**: 회사별, 카테고리별 수입/지출 요약
                    
                    ## 파일 형식
                    - **거래 내역**: CSV 파일 (거래일시,적요,입금액,출금액,거래후잔액,거래점)
                    - **분류 규칙**: JSON 파일 (회사, 카테고리, 키워드 정의)
                    
                    ## 사용 순서
                    1. `/api/v1/accounting/process`로 CSV와 JSON 파일 업로드
                    2. `/api/v1/accounting/records`로 분류된 거래 조회
                    3. `/api/v1/accounting/unclassified`로 미분류 거래 확인
                    4. `/api/v1/accounting/summary`로 통계 확인
                    """)
                .version(appVersion)
                .contact(new Contact()
                        .name("개발팀")
                        .email("rorrxr@naver.com")
                        .url("https://github.com/rorrxr/auto-accounting-processor"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}