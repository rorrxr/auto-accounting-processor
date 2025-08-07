package com.example.autoaccountingprocessor.domain.service;


import com.example.autoaccountingprocessor.domain.entity.Company;
import com.example.autoaccountingprocessor.domain.entity.Transaction;
import com.example.autoaccountingprocessor.domain.repository.CompanyRepository;
import com.example.autoaccountingprocessor.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransactionRepository transactionRepository;
    private final CompanyRepository companyRepository;

    /**
     * CSV 파일 처리 - 파싱부터 저장까지 통합
     */
    @Transactional
    public List<Transaction> processTransactions(MultipartFile file) {
        try {
            // 1. CSV 파싱
            List<Transaction> transactions = parseTransactionsFromCsv(file);

            // 2. 데이터베이스에 배치 저장
            List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);

            log.info("거래 데이터 처리 완료: {}건", savedTransactions.size());
            return savedTransactions;

        } catch (Exception e) {
            log.error("거래 처리 중 오류 발생", e);
            throw new RuntimeException("거래 처리에 실패했습니다", e);
        }
    }

    /**
     * CSV 파일에서 Transaction 엔티티로 직접 파싱
     */
    private List<Transaction> parseTransactionsFromCsv(MultipartFile file) {
        List<Transaction> transactions = new ArrayList<>();

        // 기본 회사 (실제로는 파라미터로 받거나 CSV에서 추출해야 함)
        Company defaultCompany = getDefaultCompany();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                Transaction transaction = parseLineToTransaction(line, defaultCompany);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

            log.info("CSV 파싱 완료: {}건", transactions.size());
            return transactions;

        } catch (Exception e) {
            log.error("CSV 파싱 중 오류 발생", e);
            throw new RuntimeException("CSV 파싱에 실패했습니다", e);
        }
    }

    /**
     * CSV 라인을 Transaction 엔티티로 변환
     * 형식: 거래일시,적요,입금액,출금액,거래후잔액,거래점
     */
    private Transaction parseLineToTransaction(String line, Company company) {
        try {
            String[] fields = line.split(",");
            if (fields.length != 6) {
                log.warn("잘못된 형식의 라인: {}", line);
                return null;
            }

            return Transaction.builder()
                    .company(company)
                    .occurredAt(LocalDateTime.parse(fields[0].trim(), DATE_FORMATTER))
                    .description(fields[1].trim())
                    .deposit(parseLong(fields[2].trim()))
                    .withdraw(parseLong(fields[3].trim()))
                    .balance(parseLong(fields[4].trim()))
                    .branchInfo(fields[5].trim())
                    .build();

        } catch (Exception e) {
            log.error("라인 파싱 오류: {}", line, e);
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("숫자 변환 실패: {}", value);
            return 0L;
        }
    }

    private Company getDefaultCompany() {
        return companyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("등록된 회사가 없습니다"));
    }
}