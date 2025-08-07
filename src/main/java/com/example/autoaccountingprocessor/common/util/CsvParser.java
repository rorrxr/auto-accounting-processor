package com.example.autoaccountingprocessor.common.util;

import com.example.autoaccountingprocessor.domain.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvParser {
    public static List<Transaction> parse(MultipartFile csvFile) {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                Transaction tx = new Transaction();

                String rawDate = record.get("거래일시");
                LocalDate date = LocalDate.parse(rawDate.split(" ")[0]);
                tx.setDate(date);

                tx.setDescription(record.get("적요"));

                int deposit = Integer.parseInt(record.get("입금액"));
                int withdraw = Integer.parseInt(record.get("출금액"));
                tx.setAmount(deposit - withdraw);

                transactions.add(tx);
            }

        } catch (Exception e) {
            log.error("CSV 파싱 오류", e);
            throw new RuntimeException("CSV 파싱에 실패했습니다.");
        }

        return transactions;
    }
}