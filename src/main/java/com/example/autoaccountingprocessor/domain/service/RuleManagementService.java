package com.example.autoaccountingprocessor.domain.service;


import com.example.autoaccountingprocessor.domain.entity.Category;
import com.example.autoaccountingprocessor.domain.entity.CategoryKeyword;
import com.example.autoaccountingprocessor.domain.entity.Company;
import com.example.autoaccountingprocessor.domain.repository.CategoryKeywordRepository;
import com.example.autoaccountingprocessor.domain.repository.CategoryRepository;
import com.example.autoaccountingprocessor.domain.repository.CompanyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryKeywordRepository keywordRepository;
    private final ObjectMapper objectMapper;

    /**
     * JSON 파일에서 분류 규칙 로드 (참고 레포지토리 JSON 형식과 동일)
     */
    @Transactional
    public void loadRulesFromJson(MultipartFile jsonFile) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonFile.getInputStream());
            JsonNode companiesNode = rootNode.get("companies");

            // 기존 데이터 삭제
            keywordRepository.deleteAll();
            categoryRepository.deleteAll();
            companyRepository.deleteAll();

            // 새 규칙 저장
            for (JsonNode companyNode : companiesNode) {
                processCompanyNode(companyNode);
            }

            log.info("분류 규칙 로드 완료: {}개 회사", companiesNode.size());

        } catch (Exception e) {
            log.error("JSON 규칙 로드 중 오류 발생", e);
            throw new RuntimeException("규칙 로드에 실패했습니다", e);
        }
    }

    /**
     * 회사 노드 처리 - 참고 레포지토리 방식과 동일한 구조
     */
    private void processCompanyNode(JsonNode companyNode) {
        // 1. 회사 생성
        String companyId = companyNode.get("company_id").asText();
        String companyName = companyNode.get("company_name").asText();

        Company company = Company.builder()
                .companyId(companyId)
                .companyName(companyName)
                .build();
        companyRepository.save(company);

        // 2. 카테고리들 생성
        JsonNode categoriesNode = companyNode.get("categories");
        List<Category> categories = new ArrayList<>();

        for (JsonNode categoryNode : categoriesNode) {
            String categoryId = categoryNode.get("category_id").asText();
            String categoryName = categoryNode.get("category_name").asText();

            Category category = Category.builder()
                    .categoryId(categoryId)
                    .company(company)
                    .categoryName(categoryName)
                    .build();
            categories.add(category);
        }
        categoryRepository.saveAll(categories);

        // 3. 키워드들 생성
        List<CategoryKeyword> keywords = new ArrayList<>();
        for (int i = 0; i < categoriesNode.size(); i++) {
            JsonNode categoryNode = categoriesNode.get(i);
            Category category = categories.get(i);

            JsonNode keywordsNode = categoryNode.get("keywords");
            for (JsonNode keywordNode : keywordsNode) {
                String keywordStr = keywordNode.asText();

                CategoryKeyword keyword = CategoryKeyword.builder()
                        .company(company)
                        .category(category)
                        .keyword(keywordStr)
                        .build();
                keywords.add(keyword);
            }
        }
        keywordRepository.saveAll(keywords);
    }
}