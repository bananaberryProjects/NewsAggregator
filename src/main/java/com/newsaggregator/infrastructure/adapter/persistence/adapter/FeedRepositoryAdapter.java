package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.FeedPersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter für FeedRepository.
 *
 * <p>Implementiert den Domain-Port {@link FeedRepository} und
 * delegiert die Operationen an das Spring Data JPA Repository.</p>
 */
@Component
@Transactional
public class FeedRepositoryAdapter implements FeedRepository {

    private final FeedJpaRepository jpaRepository;
    private final FeedPersistenceMapper mapper;

    public FeedRepositoryAdapter(FeedJpaRepository jpaRepository,
                                  FeedPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @SuppressWarnings("null")
    @Override
    public Feed save(Feed feed) {
        FeedJpaEntity entity;

        if (feed.getId() != null && jpaRepository.existsById(feed.getId().getValue())) {
            // Existierenden Feed aktualisieren
            Optional<FeedJpaEntity> existing = jpaRepository.findById(feed.getId().getValue());
            if (existing.isPresent()) {
                entity = existing.get();
                mapper.updateJpaEntity(entity, feed);
            } else {
                // Sollte nicht passieren, aber Fallback
                entity = mapper.toJpaEntity(feed);
            }
        } else {
            // Neuen Feed erstellen
            entity = mapper.toJpaEntity(feed);
        }

        FeedJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @SuppressWarnings("null")
    @Override
    @Transactional(readOnly = true)
    public Optional<Feed> findById(FeedId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feed> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void deleteById(FeedId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUrl(String url) {
        return jpaRepository.existsByUrl(url);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Feed> findByUrl(String url) {
        return jpaRepository.findByUrl(url)
                .map(mapper::toDomain);
    }
}
