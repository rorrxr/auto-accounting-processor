package com.example.autoaccountingprocessor.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 키워드 → 카테고리 매핑 정의 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "merchant_keyword", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"company_id", "category_id", "keyword"})
})
public class CategoryKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 키워드 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // 회사

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리

    @Column(length = 200, nullable = false)
    private String keyword; // 매칭 키워드

    private LocalDateTime createdAt; // 등록 시각

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}