package com.example.autoaccountingprocessor;

import com.example.autoaccountingprocessor.domain.dto.BankTransactionDto;
import com.example.autoaccountingprocessor.domain.dto.ClassificationResult;
import com.example.autoaccountingprocessor.domain.repository.CompanyRepository;
import com.example.autoaccountingprocessor.domain.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionClassificationServiceTest {

    @Autowired
    private TransactionClassificationService classificationService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void testClassifyTransactions() {
        // Given: 테스트용 거래 데이터 생성
        List<BankTransactionDto> testTransactions = List.of(
                BankTransactionDto.builder()
                        .transactionDate(LocalDateTime.of(2025, 7, 20, 13, 45))
                        .description("스타벅스 강남2호점")
                        .depositAmount(BigDecimal.ZERO)
                        .withdrawalAmount(new BigDecimal("5500"))
                        .balanceAfter(new BigDecimal("994500"))
                        .branch("강남지점")
                        .build(),

                BankTransactionDto.builder()
                        .transactionDate(LocalDateTime.of(2025, 7, 21, 9, 30))
                        .description("네이버페이(주)")
                        .depositAmount(new BigDecimal("150000"))
                        .withdrawalAmount(BigDecimal.ZERO)
                        .balanceAfter(new BigDecimal("1107000"))
                        .branch("온라인")
                        .build(),

                BankTransactionDto.builder()
                        .transactionDate(LocalDateTime.of(2025, 7, 21, 21, 0))
                        .description("개인용도 이체")
                        .depositAmount(BigDecimal.ZERO)
                        .withdrawalAmount(new BigDecimal("100000"))
                        .balanceAfter(new BigDecimal("1249000"))
                        .branch("강남지점")
                        .build()
        );

        // When: 거래 분류 실행
        List<ClassificationResult> results = classificationService.classifyAndSaveTransactions(testTransactions);

        // Then: 결과 검증
        assertEquals(3, results.size());

        // 스타벅스 거래 - B사 복리후생비로 분류되어야 함
        ClassificationResult starbucksResult = results.stream()
                .filter(r -> r.getDescription().contains("스타벅스"))
                .findFirst()
                .orElse(null);

        assertNotNull(starbucksResult);
        assertTrue(starbucksResult.isClassified());
        assertEquals("com_2", starbucksResult.getCompanyId());
        assertEquals("B 커머스", starbucksResult.getCompanyName());
        assertEquals("cat_204", starbucksResult.getCategoryId());
        assertEquals("복리후생비", starbucksResult.getCategoryName());
        assertEquals("스타벅스", starbucksResult.getMatchedKeyword());

        // 네이버페이 거래 - A사 매출로 분류되어야 함
        ClassificationResult naverResult = results.stream()
                .filter(r -> r.getDescription().contains("네이버페이"))
                .findFirst()
                .orElse(null);

        assertNotNull(naverResult);
        assertTrue(naverResult.isClassified());
        assertEquals("com_1", naverResult.getCompanyId());
        assertEquals("A 커머스", naverResult.getCompanyName());
        assertEquals("cat_101", naverResult.getCategoryId());
        assertEquals("매출", naverResult.getCategoryName());
        assertEquals("네이버페이", naverResult.getMatchedKeyword());

        // 개인용도 이체 - 분류되지 않아야 함
        ClassificationResult personalResult = results.stream()
                .filter(r -> r.getDescription().contains("개인용도"))
                .findFirst()
                .orElse(null);

        assertNotNull(personalResult);
        assertFalse(personalResult.isClassified());
        assertNotNull(personalResult.getFailureReason());

        // 데이터베이스 저장 확인
        assertEquals(3, transactionRepository.count());
        assertEquals(2, transactionRepository.findByIsClassifiedFalse().size() - 1); // 분류 성공 2건, 실패 1건
    }
}