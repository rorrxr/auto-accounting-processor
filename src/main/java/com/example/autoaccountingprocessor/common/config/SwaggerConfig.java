package com.example.autoaccountingprocessor.common.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("자동 회계 분류 API")
                        .version("v1")
                        .description("CSV + 규칙 파일 기반 거래 분류 시스템")
                );
    }

    @Bean
    public GroupedOpenApi accountingApi() {
        return GroupedOpenApi.builder()
                .group("accounting")
                .pathsToMatch("/api/v1/accounting/**")
                .build();
    }
}