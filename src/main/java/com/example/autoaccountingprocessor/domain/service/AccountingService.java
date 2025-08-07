package com.example.autoaccountingprocessor.domain.service;

import com.example.autoaccountingprocessor.domain.dto.CategorySummaryResponse;
import com.example.autoaccountingprocessor.domain.dto.ClassifiedTransactionResponse;
import com.example.autoaccountingprocessor.domain.dto.ProcessingResultResponse;
import com.example.autoaccountingprocessor.domain.dto.UnclassifiedTransactionResponse;
import com.example.autoaccountingprocessor.domain.entity.*;
import com.example.autoaccountingprocessor.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingService {

    private final TransactionProcessingService transactionProcessingService;
    private final ClassificationService classificationService;
    private final RuleManagementService ruleManagementService;

    private final ClassifiedTransactionRepository classifiedTransactionRepository;
    private final UnclassifiedTransactionRepository unclassifiedTransactionRepository;
    private final CompanyRepository companyRepository;

    /**
     * CSV 거래내역과 JSON 규칙을 받아 통합 처리 (참고 레포지토리 API와 동일한 방식)
     */
    @Transactional
    public ProcessingResultResponse processAccountingFiles(MultipartFile transactionsFile,
                                                           MultipartFile rulesFile) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("회계 처리 시작 - 거래내역: {}, 규칙: {}",
                    transactionsFile.getOriginalFilename(),
                    rulesFile.getOriginalFilename());

            // 1. 분류 규칙 로드
            ruleManagementService.loadRulesFromJson(rulesFile);

            // 2. 거래 내역 파싱 및 저장
            List<Transaction> savedTransactions = transactionProcessingService.processTransactions(transactionsFile);

            // 3. 거래 분류 실행
            int classifiedCount = classificationService.classifyTransactions(savedTransactions);

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("회계 처리 완료 - 총 {}건 중 {}건 분류 성공 ({}ms 소요)",
                    savedTransactions.size(), classifiedCount, processingTime);

            return ProcessingResultResponse.success(savedTransactions.size(), classifiedCount, processingTime);

        } catch (Exception e) {
            log.error("회계 처리 중 오류 발생", e);
            throw new RuntimeException("회계 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 회사의 분류된 거래 조회 (참고 레포지토리 API와 동일)
     */
    public List<ClassifiedTransactionResponse> getClassifiedRecords(String companyId) {
        return classifiedTransactionRepository.findByCompanyCompanyIdOrderByClassifiedAtDesc(companyId)
                .stream()
                .map(ClassifiedTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 회사의 분류된 거래 조회 (페이징)
     */
    public Page<ClassifiedTransactionResponse> getClassifiedRecords(String companyId, Pageable pageable) {
        return classifiedTransactionRepository.findByCompanyCompanyId(companyId, pageable)
                .map(ClassifiedTransactionResponse::from);
    }

    /**
     * 특정 회사의 미분류 거래 조회 (참고 레포지토리 API와 동일)
     */
    public List<UnclassifiedTransactionResponse> getUnclassifiedRecords(String companyId) {
        return unclassifiedTransactionRepository.findByCompanyCompanyIdOrderById(companyId)
                .stream()
                .map(UnclassifiedTransactionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 회사의 전체 수입/지출 요약 (참고 레포지토리 API와 동일)
     */
    public CategorySummaryResponse getTotalSummary(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다: " + companyId));

        Long totalIncome = classifiedTransactionRepository.getTotalDepositByCompany(companyId);
        Long totalExpenditure = classifiedTransactionRepository.getTotalWithdrawByCompany(companyId);
        long recordCount = classifiedTransactionRepository.countByCompanyId(companyId);

        return CategorySummaryResponse.total(company.getCompanyName(), totalIncome, totalExpenditure, recordCount);
    }

    /**
     * 특정 회사의 카테고리별 수입/지출 요약 (참고 레포지토리 API와 동일)
     */
    public List<CategorySummaryResponse> getCategorySummaries(String companyId) {
        return classificationService.getCategorySummaries(companyId);
    }
}