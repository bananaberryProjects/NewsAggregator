package com.newsaggregator.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.domain.port.out.RssFeedReader;
import com.newsaggregator.domain.service.TitleSimilarityService;
import com.newsaggregator.infrastructure.adapter.persistence.adapter.ArticleRepositoryAdapter;
import com.newsaggregator.infrastructure.adapter.persistence.adapter.CryptoPriceRepositoryAdapter;
import com.newsaggregator.infrastructure.adapter.persistence.adapter.FeedRepositoryAdapter;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.ArticlePersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.FeedPersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CategoryJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CryptoPriceJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;
import com.newsaggregator.infrastructure.adapter.rss.RssFeedReaderAdapter;
import com.newsaggregator.infrastructure.adapter.scheduler.CoinGeckoScheduledTask;

/**
 * Spring Configuration für Dependency Injection.
 *
 * <p>Diese Klasse konfiguriert die Beans für die hexagonale Architektur.
 * Sie stellt sicher, dass die Ports mit den richtigen Adaptern verbunden werden.</p>
 */
@Configuration
@EnableScheduling
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        return mapper;
    }

    @Bean
    public CryptoPriceRepository cryptoPriceRepository(CryptoPriceJpaRepository jpaRepository) {
        return new CryptoPriceRepositoryAdapter(jpaRepository);
    }

    @Bean
    public CoinGeckoScheduledTask coinGeckoScheduledTask(RestTemplate restTemplate, ObjectMapper objectMapper, CryptoPriceRepository cryptoPriceRepository) {
        return new CoinGeckoScheduledTask(restTemplate, objectMapper, cryptoPriceRepository);
    }

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
            ArticlePersistenceMapper mapper,
            TitleSimilarityService titleSimilarityService) {
        return new ArticleRepositoryAdapter(jpaRepository, feedJpaRepository, mapper, titleSimilarityService);
    }

    // ==================== RSS Reader Adapter ====================

    @Bean
    public RssFeedReader rssFeedReader() {
        return new RssFeedReaderAdapter();
    }
}
