package com.newsaggregator.infrastructure.config;

import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.domain.port.out.RssFeedReader;
import com.newsaggregator.infrastructure.adapter.persistence.adapter.ArticleRepositoryAdapter;
import com.newsaggregator.infrastructure.adapter.persistence.adapter.FeedRepositoryAdapter;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.ArticlePersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.FeedPersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CategoryJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;
import com.newsaggregator.infrastructure.adapter.rss.RssFeedReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Configuration für Dependency Injection.
 *
 * <p>Diese Klasse konfiguriert die Beans für die hexagonale Architektur.
 * Sie stellt sicher, dass die Ports mit den richtigen Adaptern verbunden werden.</p>
 */
@Configuration
@EnableScheduling
public class BeanConfig {

    // ==================== Repository Adapters ====================

    @Bean
    public FeedRepository feedRepository(
            FeedJpaRepository jpaRepository,
            CategoryJpaRepository categoryJpaRepository,
            FeedPersistenceMapper mapper) {
        return new FeedRepositoryAdapter(jpaRepository, categoryJpaRepository, mapper);
    }

    @Bean
    public ArticleRepository articleRepository(
            ArticleJpaRepository jpaRepository,
            FeedJpaRepository feedJpaRepository,
            ArticlePersistenceMapper mapper) {
        return new ArticleRepositoryAdapter(jpaRepository, feedJpaRepository, mapper);
    }

    // ==================== RSS Reader Adapter ====================

    @Bean
    public RssFeedReader rssFeedReader() {
        return new RssFeedReaderAdapter();
    }
}
