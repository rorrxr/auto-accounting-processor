package com.example.autoaccountingprocessor.domain.service;

import com.example.autoaccountingprocessor.domain.dto.CategorySummaryResponse;
import com.example.autoaccountingprocessor.domain.entity.*;
import com.example.autoaccountingprocessor.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final CategoryKeywordRepository keywordRepository;
    private final ClassifiedTransactionRepository classifiedRepository;
    private final UnclassifiedTransactionRepository unclassifiedRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 거래 리스트를 분류하여 저장 (참고 레포지토리 방식과 동일)
     */
    @Transactional
    public int classifyTransactions(List<Transaction> transactions) {
        List<CategoryKeyword> allKeywords = keywordRepository.findAllWithDetails();

        int classifiedCount = 0;

        for (Transaction transaction : transactions) {
            CategoryKeyword matchedKeyword = findMatchingKeyword(transaction, allKeywords);

            if (matchedKeyword != null) {
                // 분류 성공 - 참고 레포지토리와 동일한 방식
                ClassifiedTransaction classified = ClassifiedTransaction.from(transaction, matchedKeyword);
                classifiedRepository.save(classified);
                classifiedCount++;

                log.debug("분류 성공: {} -> {}/{}",
                        transaction.getDescription(),
                        matchedKeyword.getCompany().getCompanyName(),
                        matchedKeyword.getCategory().getCategoryName());
            } else {
                // 분류 실패 - 참고 레포지토리와 동일한 방식
                UnclassifiedTransaction unclassified = UnclassifiedTransaction.from(
                        transaction,
                        transaction.getCompany(),
                        "키워드 미일치"
                );
                unclassifiedRepository.save(unclassified);

                log.debug("분류 실패: {}", transaction.getDescription());
            }
        }

        log.info("분류 완료: 총 {}건 중 {}건 성공", transactions.size(), classifiedCount);
        return classifiedCount;
    }

    /**
     * 거래 설명에서 매칭되는 키워드 찾기
     */
    private CategoryKeyword findMatchingKeyword(Transaction transaction,
                                                      List<CategoryKeyword> keywords) {
        String description = transaction.getDescription();

        return keywords.stream()
                .filter(keyword -> description.contains(keyword.getKeyword()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 특정 회사의 카테고리별 요약 통계 생성 (참고 레포지토리 방식)
     */
    public List<CategorySummaryResponse> getCategorySummaries(String companyId) {
        List<Category> categories = categoryRepository.findByCompanyCompanyIdOrderByCreatedAtDesc(companyId);

        return categories.stream()
                .map(category -> {
                    Long totalIncome = classifiedRepository.getTotalDepositByCategory(category.getCategoryId());
                    Long totalExpenditure = classifiedRepository.getTotalWithdrawByCategory(category.getCategoryId());
                    long recordCount = classifiedRepository.countByCategoryId(category.getCategoryId());

                    return CategorySummaryResponse.of(
                            category.getCategoryId(),
                            category.getCategoryName(),
                            totalIncome,
                            totalExpenditure,
                            recordCount
                    );
                })
                .collect(Collectors.toList());
    }
}