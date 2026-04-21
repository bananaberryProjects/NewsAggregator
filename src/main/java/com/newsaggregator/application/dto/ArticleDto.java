package com.newsaggregator.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für Article-Informationen.
 *
 * <p>Wird für die Kommunikation zwischen Application Layer und Web Layer verwendet.
 * Enthält keine Domain-Logik, nur Daten.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    private Long id;
    private String title;
    private String description;
    private String link;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Long feedId;
    private String feedName;
    private List<String> categoryIds;
    private String contentHtml;

}
