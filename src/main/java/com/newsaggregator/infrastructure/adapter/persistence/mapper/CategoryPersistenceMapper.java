package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.infrastructure.adapter.persistence.entity.CategoryEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class CategoryPersistenceMapper {

    public static Category toDomain(CategoryEntity entity) {
        return new Category(
                new CategoryId(entity.getId()),
                entity.getName(),
                entity.getColor(),
                entity.getIcon()
        );
    }

    public static CategoryEntity toEntity(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId().getValue());
        entity.setName(category.getName());
        entity.setColor(category.getColor());
        entity.setIcon(category.getIcon());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}
