package com.newsaggregator.application.mapper;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.domain.model.Article;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper für die Konvertierung zwischen Article (Domain) und ArticleDto.
 */
@Component
public class ArticleMapper {

    /**
     * Wandelt ein Domain-Objekt in ein DTO um.
     */
    public ArticleDto toDto(Article article) {
        if (article == null) {
            return null;
        }

        // Kategorien vom Feed holen
        List<String> categoryIds = null;
        if (article.getFeed() != null && article.getFeed().getCategoryIds() != null) {
            categoryIds = article.getFeed().getCategoryIds().stream()
                    .map(catId -> catId.getValue().toString())
                    .collect(Collectors.toList());
        }

        return ArticleDto.builder()
                .id(article.getId() != null ? article.getId().getValue() : null)
                .title(article.getTitle())
                .description(article.getDescription())
                .link(article.getLink())
                .imageUrl(article.getImageUrl())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .feedId(article.getFeed() != null && article.getFeed().getId() != null
                        ? article.getFeed().getId().getValue() : null)
                .feedName(article.getFeed() != null ? article.getFeed().getName() : null)
                .categoryIds(categoryIds)
                .contentHtml(article.getExtractedContent())
                .extractionFailed(article.isExtractionFailed())
                .readingTimeMinutes(calculateReadingTime(article))
                .build();
    }

    /**
     * Berechnet die geschätzte Lesezeit in Minuten.
     * Annahme: 200 Wörter pro Minute.
     */
    private int calculateReadingTime(Article article) {
        String text = (article.getTitle() != null ? article.getTitle() + " " : "")
                + (article.getDescription() != null ? article.getDescription() + " " : "")
                + (article.getExtractedContent() != null ? article.getExtractedContent() : "");

        if (text.isBlank()) {
            return 1;
        }

        // Einfache Word-Count: Splitten nach Whitespace
        String[] words = text.trim().split("\\s+");
        int wordCount = words.length;
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }
}
