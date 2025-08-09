package com.example.autoaccountingprocessor.domain.service;


import com.example.autoaccountingprocessor.domain.entity.Category;
import com.example.autoaccountingprocessor.domain.entity.CategoryKeyword;
import com.example.autoaccountingprocessor.domain.entity.Company;
import com.example.autoaccountingprocessor.domain.repository.CategoryKeywordRepository;
import com.example.autoaccountingprocessor.domain.repository.CategoryRepository;
import com.example.autoaccountingprocessor.domain.repository.ClassifiedTransactionRepository;
import com.example.autoaccountingprocessor.domain.repository.CompanyRepository;
import com.example.autoaccountingprocessor.domain.repository.TransactionRepository;
import com.example.autoaccountingprocessor.domain.repository.UnclassifiedTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryKeywordRepository keywordRepository;
    private final ClassifiedTransactionRepository classifiedTransactionRepository;
    private final UnclassifiedTransactionRepository unclassifiedTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    /**
     * JSON 파일에서 분류 규칙 로드 (참고 레포지토리 JSON 형식과 동일)
     */
    @Transactional
    public void loadRulesFromJson(MultipartFile jsonFile) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonFile.getInputStream());
            JsonNode companiesNode = rootNode.get("companies");

            // 기존 데이터 삭제 - 외래 키 제약 조건을 고려한 올바른 순서로 삭제
            log.info("기존 데이터 삭제 시작");

            // 1단계: 분류/미분류 거래 내역 삭제 (가장 하위 테이블부터)
            classifiedTransactionRepository.deleteAll();
            unclassifiedTransactionRepository.deleteAll();
            
            // 2단계: 거래 내역 삭제
            transactionRepository.deleteAll();
            
            // 3단계: 키워드 삭제
            keywordRepository.deleteAll();
            
            // 4단계: 카테고리 삭제
            categoryRepository.deleteAll();
            
            // 5단계: 회사 삭제 (최상위 테이블)
            companyRepository.deleteAll();
            
            // 삭제 후 영속성 컨텍스트 플러시 및 클리어
            entityManager.flush();
            entityManager.clear();

            log.info("기존 데이터 삭제 완료");

            // 새 규칙 저장
            log.info("새 규칙 저장 시작: {}개 회사", companiesNode.size());
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
     * 회사 노드 처리 - Native SQL을 사용한 안전한 저장 방식
     */
    private void processCompanyNode(JsonNode companyNode) {
        try {
            // 1. 회사 생성 및 저장
            String companyId = companyNode.get("company_id").asText();
            String companyName = companyNode.get("company_name").asText();

            // Native SQL을 사용한 직접 삽입
            entityManager.createNativeQuery(
                "INSERT INTO company (company_id, company_name, created_at) VALUES (?, ?, NOW())")
                .setParameter(1, companyId)
                .setParameter(2, companyName)
                .executeUpdate();
            
            entityManager.flush();
            
            log.info("회사 저장 완료: {} ({})", companyName, companyId);

            // 2. 카테고리들 생성 및 저장
            JsonNode categoriesNode = companyNode.get("categories");
            List<String> categoryIds = new ArrayList<>();

            for (JsonNode categoryNode : categoriesNode) {
                String categoryId = categoryNode.get("category_id").asText();
                String categoryName = categoryNode.get("category_name").asText();

                // Native SQL로 카테고리 삽입
                entityManager.createNativeQuery(
                    "INSERT INTO category (category_id, company_id, category_name, created_at) VALUES (?, ?, ?, NOW())")
                    .setParameter(1, categoryId)
                    .setParameter(2, companyId)
                    .setParameter(3, categoryName)
                    .executeUpdate();
                
                categoryIds.add(categoryId);
            }
            
            entityManager.flush();
            log.info("카테고리 저장 완료: {}개", categoryIds.size());

            // 3. 키워드들 생성 및 저장
            int keywordCount = 0;
            for (int i = 0; i < categoriesNode.size(); i++) {
                JsonNode categoryNode = categoriesNode.get(i);
                String categoryId = categoryIds.get(i);

                JsonNode keywordsNode = categoryNode.get("keywords");
                for (JsonNode keywordNode : keywordsNode) {
                    String keywordStr = keywordNode.asText();

                    // Native SQL로 키워드 삽입
                    entityManager.createNativeQuery(
                        "INSERT INTO merchant_keyword (company_id, category_id, keyword, created_at) VALUES (?, ?, ?, NOW())")
                        .setParameter(1, companyId)
                        .setParameter(2, categoryId)
                        .setParameter(3, keywordStr)
                        .executeUpdate();
                    
                    keywordCount++;
                }
            }
            
            entityManager.flush();
            log.info("키워드 저장 완료: {}개", keywordCount);
            
        } catch (Exception e) {
            log.error("회사 노드 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("회사 데이터 저장에 실패했습니다", e);
        }
    }
}