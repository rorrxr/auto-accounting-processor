package com.example.autoaccountingprocessor.domain.service;

import com.example.autoaccountingprocessor.common.util.CsvParser;
import com.example.autoaccountingprocessor.domain.dto.AccountingRecordResponseDto;
import com.example.autoaccountingprocessor.domain.entity.Category;
import com.example.autoaccountingprocessor.domain.entity.Company;
import com.example.autoaccountingprocessor.domain.entity.Transaction;
import com.example.autoaccountingprocessor.domain.repository.CategoryRepository;
import com.example.autoaccountingprocessor.domain.repository.CompanyRepository;
import com.example.autoaccountingprocessor.domain.repository.TransactionRepository;
import com.example.autoaccountingprocessor.common.util.RuleLoader;
import com.example.autoaccountingprocessor.domain.rule.CategoryRule;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AccountingService {

    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private static final String UNCATEGORIZED = "미분류";

    public void process(MultipartFile csvFile, MultipartFile rulesFile) {
        List<Transaction> transactions = CsvParser.parse(csvFile);
        Map<String, Map<String, CategoryRule>> rules = RuleLoader.load(rulesFile);

        for (Transaction tx : transactions) {
            String companyId = findCompanyId(tx.getDescription(), rules);
            Company company = null;
            Category category;

            // 회사 ID가 식별되면
            if (companyId != null) {
                company = companyRepository.findById(companyId).orElse(null);
                Map<String, CategoryRule> companyRules = rules.get(companyId);
                CategoryRule categoryRule = findCategoryRule(tx.getDescription(), companyRules);

                if (categoryRule != null) {
                    category = categoryRepository.findByName(categoryRule.getCategoryName())
                            .orElseGet(() -> categoryRepository.save(new Category(categoryRule.getCategoryName())));
                } else {
                    category = getUncategorizedCategory();
                }
            } else {
                category = getUncategorizedCategory();
            }

            tx.setCompany(company);    // company는 null일 수 있음 (귀속 불가)
            tx.setCategory(category);  // 반드시 존재

            transactionRepository.save(tx);
        }
    }

    public List<AccountingRecordResponseDto> getRecordsByCompany(String companyId) {
        return transactionRepository.findByCompanyId(companyId).stream()
                .map(AccountingRecordResponseDto::from)
                .collect(Collectors.toList());
    }

    // 설명에 포함된 키워드로 회사 ID 찾기
    private String findCompanyId(String description, Map<String, Map<String, CategoryRule>> rules) {
        for (Map.Entry<String, Map<String, CategoryRule>> entry : rules.entrySet()) {
            String companyId = entry.getKey();
            Map<String, CategoryRule> keywords = entry.getValue();

            for (String keyword : keywords.keySet()) {
                if (description.contains(keyword)) {
                    return companyId;
                }
            }
        }
        return null;
    }

    // 설명에 포함된 키워드로 계정과목 Rule 찾기
    private CategoryRule findCategoryRule(String description, Map<String, CategoryRule> ruleMap) {
        if (ruleMap == null) return null;

        for (Map.Entry<String, CategoryRule> entry : ruleMap.entrySet()) {
            if (description.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    // '미분류' Category를 반환 (없으면 생성)
    private Category getUncategorizedCategory() {
        return categoryRepository.findByName(UNCATEGORIZED)
                .orElseGet(() -> categoryRepository.save(new Category(UNCATEGORIZED)));
    }
}