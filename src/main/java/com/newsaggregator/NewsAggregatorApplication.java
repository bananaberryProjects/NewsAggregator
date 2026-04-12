package com.newsaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * News Aggregator Application
 *
 * <p>Die Anwendung folgt der hexagonalen Architektur:</p>
 * <ul>
 *   <li><b>domain:</b> Geschäftslogik, Entities, Ports</li>
 *   <li><b>application:</b> Use Cases, Application Services, DTOs</li>
 *   <li><b>infrastructure:</b> Adapters (Persistence, Web, RSS)</li>
 * </ul>
 */
@SpringBootApplication
@EntityScan(basePackages = "com.newsaggregator.infrastructure.adapter.persistence.entity")
@EnableJpaRepositories(basePackages = "com.newsaggregator.infrastructure.adapter.persistence.repository")
public class NewsAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsAggregatorApplication.class, args);
    }
}
