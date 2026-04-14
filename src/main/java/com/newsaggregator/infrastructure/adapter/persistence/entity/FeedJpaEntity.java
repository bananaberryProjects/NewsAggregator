package com.newsaggregator.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity für Feed.
 *
 * <p>Diese Klasse repräsentiert ein Feed in der Datenbank.
 * Sie enthält JPA-Annotationen und hat keine Business-Logik.
 * Die Umwandlung zu/von Domain-Objekten erfolgt durch Mapper.</p>
 */
@Entity
@Table(name = "feeds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(length = 1000)
    private String description;

    @Column(name = "last_fetched")
    private LocalDateTime lastFetched;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FeedStatus status = FeedStatus.ACTIVE;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArticleJpaEntity> articles = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "feed_categories",
        joinColumns = @JoinColumn(name = "feed_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<CategoryEntity> categories = new ArrayList<>();

    public enum FeedStatus {
        ACTIVE, ERROR, DISABLED
    }

}
