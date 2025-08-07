package com.example.autoaccountingprocessor.domain.service;

import com.example.autoaccountingprocessor.domain.dto.BankTransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CsvParsingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * CSV 파일을 파싱하여 BankTransactionDto 리스트로 변환
     */
    public List<BankTransactionDto> parseCsvFile(MultipartFile file) {
        List<BankTransactionDto> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // 헤더 라인 건너뛰기
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                BankTransactionDto transaction = parseLine(line);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

            log.info("CSV 파일 파싱 완료. 총 {}건의 거래 내역을 처리했습니다.", transactions.size());

        } catch (Exception e) {
            log.error("CSV 파일 파싱 중 오류 발생", e);
            throw new RuntimeException("CSV 파일 파싱에 실패했습니다.", e);
        }

        return transactions;
    }

    /**
     * CSV 라인을 파싱하여 BankTransactionDto 객체로 변환
     * 예상 형식: 거래일시,적요,입금액,출금액,거래후잔액,거래점
     */
    private BankTransactionDto parseLine(String line) {
        try {
            // CSV 라인 분할 (간단한 구현 - 실제로는 더 정교한 CSV 파싱 라이브러리 사용 권장)
            String[] fields = line.split(",");

            if (fields.length != 6) {
                log.warn("잘못된 형식의 라인: {}", line);
                return null;
            }

            LocalDateTime transactionDate = LocalDateTime.parse(fields[0].trim(), DATE_FORMATTER);
            String description = fields[1].trim();
            BigDecimal depositAmount = parseBigDecimal(fields[2].trim());
            BigDecimal withdrawalAmount = parseBigDecimal(fields[3].trim());
            BigDecimal balanceAfter = parseBigDecimal(fields[4].trim());
            String branch = fields[5].trim();

            return BankTransactionDto.builder()
                    .transactionDate(transactionDate)
                    .description(description)
                    .depositAmount(depositAmount)
                    .withdrawalAmount(withdrawalAmount)
                    .balanceAfter(balanceAfter)
                    .branch(branch)
                    .build();

        } catch (Exception e) {
            log.error("라인 파싱 중 오류 발생: {}", line, e);
            return null;
        }
    }

    /**
     * 문자열을 BigDecimal로 변환 (0일 경우 null 반환하지 않고 ZERO 반환)
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("숫자 변환 실패: {}", value);
            return BigDecimal.ZERO;
        }
    }
}