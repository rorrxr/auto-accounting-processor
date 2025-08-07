package com.example.autoaccountingprocessor.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * 거래 원본 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 거래 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // 회사

    private LocalDateTime occurredAt; // 거래 발생 일시
    private String description; // 거래 설명
    private Long deposit; // 입금액
    private Long withdraw; // 출금액
    private Long balance; // 거래 후 잔액
    private String branchInfo; // 거래 지점 정보
    private LocalDateTime createdAt; // 생성 시각

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}