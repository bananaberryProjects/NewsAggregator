package com.newsaggregator.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity fuer KI-Summary-Cache.
 * Speichert generierte KI-Zusammenfassungen pro Tag.
 */
@Entity
@Table(name = "ai_summary_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryCacheJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "for_date", nullable = false, unique = true)
    private LocalDate forDate;

    @Column(name = "summary_json", nullable = false, columnDefinition = "TEXT")
    private String summaryJson;

    @Column(name = "is_fallback", nullable = false)
    private boolean fallback;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
