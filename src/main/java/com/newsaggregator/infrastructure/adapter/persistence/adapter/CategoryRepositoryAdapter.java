package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.newsaggregator.domain.port.out.CategoryRepository;
import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.infrastructure.adapter.persistence.entity.CategoryEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.CategoryPersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CategoryJpaRepository;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll().stream()
                .map(CategoryPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.getValue())
                .map(CategoryPersistenceMapper::toDomain);
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = CategoryPersistenceMapper.toEntity(category);
        CategoryEntity saved = jpaRepository.save(entity);
        return CategoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(CategoryId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
