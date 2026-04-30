package com.newsaggregator.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity fuer KI-Wetter-Insight-Cache.
 * Speichert generierte Wetter-Insights pro Standort.
 */
@Entity
@Table(name = "weather_insight_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherInsightCacheJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_key", nullable = false, length = 50)
    private String locationKey; // "lat,lon" z.B. "52.52,13.41"

    @Column(name = "insight_json", nullable = false, columnDefinition = "TEXT")
    private String insightJson;

    @Column(name = "is_fallback", nullable = false)
    private boolean fallback;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
