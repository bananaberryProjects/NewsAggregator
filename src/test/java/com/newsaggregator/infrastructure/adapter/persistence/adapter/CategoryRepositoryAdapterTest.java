package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.infrastructure.adapter.persistence.entity.CategoryEntity;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CategoryJpaRepository;

/**
 * Unit-Test für CategoryRepositoryAdapter.
 *
 * <p>Testet die Konvertierung zwischen Domain-Objekten und JPA Entities.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @Mock
    private CategoryJpaRepository jpaRepository;

    private CategoryRepositoryAdapter adapter;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        adapter = new CategoryRepositoryAdapter(jpaRepository);
    }

    @SuppressWarnings("null")
    @Test
    void findById_ShouldReturnCategory_WhenExists() {
        // Given
        UUID categoryId = UUID.randomUUID();
        CategoryEntity jpaEntity = createCategoryEntity(categoryId, "Technology");
        CategoryId domainId = new CategoryId(categoryId);

        when(jpaRepository.findById(categoryId)).thenReturn(Optional.of(jpaEntity));

        // When
        Optional<Category> result = adapter.findById(domainId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Technology", result.get().getName());
        assertEquals(categoryId, result.get().getId().getValue());
        verify(jpaRepository).findById(categoryId);
    }

    @SuppressWarnings("null")
    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        UUID categoryId = UUID.randomUUID();
        CategoryId domainId = new CategoryId(categoryId);
        when(jpaRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = adapter.findById(domainId);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository).findById(categoryId);
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        CategoryEntity entity1 = createCategoryEntity(id1, "Technology");
        CategoryEntity entity2 = createCategoryEntity(id2, "Sports");

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        // When
        List<Category> result = adapter.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Technology", result.get(0).getName());
        assertEquals("Sports", result.get(1).getName());
        verify(jpaRepository).findAll();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoCategories() {
        // Given
        when(jpaRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Category> result = adapter.findAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("null")
    @Test
    void save_ShouldPersistNewCategory() {
        // Given
        UUID categoryId = UUID.randomUUID();
        Category category = Category.create("Technology", "#667eea", "icon");
        CategoryEntity savedEntity = createCategoryEntity(categoryId, "Technology");

        when(jpaRepository.save(any(CategoryEntity.class))).thenReturn(savedEntity);

        // When
        Category result = adapter.save(category);

        // Then
        assertNotNull(result);
        assertEquals("Technology", result.getName());
        assertEquals(categoryId, result.getId().getValue());
        verify(jpaRepository).save(any(CategoryEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    void save_ShouldUpdateExistingCategory() {
        // Given
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(new CategoryId(categoryId), "Updated Technology", "#667eea", "icon");
        CategoryEntity savedEntity = createCategoryEntity(categoryId, "Updated Technology");

        when(jpaRepository.save(any(CategoryEntity.class))).thenReturn(savedEntity);

        // When
        Category result = adapter.save(category);

        // Then
        assertNotNull(result);
        assertEquals("Updated Technology", result.getName());
        assertEquals(categoryId, result.getId().getValue());
        verify(jpaRepository).save(any(CategoryEntity.class));
    }

    @SuppressWarnings("null")
    @Test
    void deleteById_ShouldDeleteCategory() {
        // Given
        UUID categoryId = UUID.randomUUID();
        CategoryId domainId = new CategoryId(categoryId);

        // When
        adapter.deleteById(domainId);

        // Then
        verify(jpaRepository).deleteById(categoryId);
    }

    // Hilfsmethoden

    private CategoryEntity createCategoryEntity(UUID id, String name) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setColor("#667eea");
        entity.setIcon("icon");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}