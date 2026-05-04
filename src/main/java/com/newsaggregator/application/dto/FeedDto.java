package com.newsaggregator.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für Feed-Informationen.
 *
 * <p>Wird für die Kommunikation zwischen Application Layer und Web Layer verwendet.
 * Enthält keine Domain-Logik, nur Daten.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDto {

    private Long id;
    private String name;
    private String url;
    private String description;
    private LocalDateTime lastFetchedAt;
    private LocalDateTime createdAt;
    private String status;
    private int articleCount;
    private List<String> categoryIds;
    private Boolean extractContent;
    private List<String> blockedKeywords;

}
