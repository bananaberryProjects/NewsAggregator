package com.newsaggregator.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity für Article.
 *
 * <p>Diese Klasse repräsentiert einen Artikel in der Datenbank.
 * Sie enthält JPA-Annotationen und hat keine Business-Logik.
 * Die Umwandlung zu/von Domain-Objekten erfolgt durch Mapper.</p>
 */
@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false, unique = true, length = 1000)
    private String link;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private FeedJpaEntity feed;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

}
