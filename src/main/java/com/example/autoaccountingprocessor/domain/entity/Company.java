package com.example.autoaccountingprocessor.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 회사 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @Column(length = 50)
    private String companyId; // 고유 ID

    @Column(length = 100, nullable = false)
    private String companyName; // 회사 이름

    private LocalDateTime createdAt; // 생성 시각

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}