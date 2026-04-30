package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper für die Konvertierung zwischen Feed (Domain) und FeedJpaEntity.
 */
@Component
public class FeedPersistenceMapper {

    private final ObjectMapper objectMapper;
    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    public FeedPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Wandelt eine JPA Entity in ein Domain-Objekt um.
     */
    public Feed toDomain(FeedJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<String> keywords = parseBlockedKeywords(entity.getBlockedKeywords());

        Feed feed = Feed.of(
                FeedId.of(entity.getId()),
                entity.getName(),
                entity.getUrl(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getLastFetched(),
                mapStatus(entity.getStatus()),
                entity.getExtractContent() != null ? entity.getExtractContent() : true,
                keywords
        );

        // Kategorien zuweisen
        if (entity.getCategories() != null) {
            List<CategoryId> categoryIds = entity.getCategories().stream()
                    .map(cat -> CategoryId.of(cat.getId().toString()))
                    .collect(Collectors.toList());
            feed.setCategories(categoryIds);
        }

        return feed;
    }

    /**
     * Wandelt ein Domain-Objekt in eine JPA Entity um.
     * Bei neuen Feeds (ohne ID) wird die ID nicht gesetzt.
     */
    public FeedJpaEntity toJpaEntity(Feed domain) {
        if (domain == null) {
            return null;
        }

        FeedJpaEntity entity = new FeedJpaEntity();

        // ID nur setzen, wenn vorhanden (nicht bei neuen Feeds)
        if (domain.getId() != null) {
            entity.setId(domain.getId().getValue());
        }

        entity.setName(domain.getName());
        entity.setUrl(domain.getUrl());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastFetched(domain.getLastFetched());
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setExtractContent(domain.isExtractContent());
        entity.setBlockedKeywords(serializeBlockedKeywords(domain.getBlockedKeywords()));

        return entity;
    }

    /**
     * Aktualisiert eine bestehende JPA Entity mit Werten aus dem Domain-Objekt.
     */
    public void updateJpaEntity(FeedJpaEntity entity, Feed domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setName(domain.getName());
        entity.setUrl(domain.getUrl());
        entity.setDescription(domain.getDescription());
        entity.setLastFetched(domain.getLastFetched());
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setExtractContent(domain.isExtractContent());
        entity.setBlockedKeywords(serializeBlockedKeywords(domain.getBlockedKeywords()));
        // createdAt und ID werden nicht aktualisiert
    }

    // ==================== Hilfsmethoden ====================

    private List<String> parseBlockedKeywords(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("null")) {
            return new ArrayList<>();
        }
        // Versuche echtes JSON-Array zu parsen (z.B. ["stau","reisewarnung"])
        try {
            List<String> parsed = objectMapper.readValue(raw, STRING_LIST_TYPE);
            // Edge case: DB hat ["stau, reisewarnung"] (ein String mit Komma statt zwei Elementen)
            if (parsed.size() == 1 && parsed.get(0).contains(",")) {
                List<String> exploded = new ArrayList<>();
                for (String part : parsed.get(0).split(",")) {
                    String kw = part.trim().replace("\"", "");
                    if (!kw.isEmpty()) exploded.add(kw.toLowerCase());
                }
                if (exploded.size() > 1) return exploded;
            }
            return parsed.stream()
                    .map(k -> k.replace("\"", "").trim().toLowerCase())
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Manueller Fallback: entferne [ ] und ", dann per Komma splitten
            String cleaned = raw.trim();
            if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            cleaned = cleaned.replace("\"", "");
            List<String> result = new ArrayList<>();
            for (String part : cleaned.split(",")) {
                String kw = part.trim();
                if (!kw.isEmpty()) {
                    result.add(kw.toLowerCase());
                }
            }
            return result;
        }
    }

    private String serializeBlockedKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(keywords);
        } catch (Exception e) {
            return null;
        }
    }

    private FeedStatus mapStatus(FeedJpaEntity.FeedStatus status) {
        if (status == null) {
            return FeedStatus.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> FeedStatus.ACTIVE;
            case ERROR -> FeedStatus.ERROR;
            case DISABLED -> FeedStatus.DISABLED;
        };
    }

    private FeedJpaEntity.FeedStatus mapStatus(FeedStatus status) {
        if (status == null) {
            return FeedJpaEntity.FeedStatus.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> FeedJpaEntity.FeedStatus.ACTIVE;
            case ERROR -> FeedJpaEntity.FeedStatus.ERROR;
            case DISABLED -> FeedJpaEntity.FeedStatus.DISABLED;
        };
    }
}
