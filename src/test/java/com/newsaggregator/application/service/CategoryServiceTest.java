package com.newsaggregator.application.service;

import java.util.List;

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
import com.newsaggregator.domain.port.out.CategoryRepository;

/**
 * Unit-Test für CategoryService.
 *
 * <p>Testet die Geschäftslogik für Kategorien.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService service;

    private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new CategoryService(categoryRepository);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        Category cat1 = Category.create("Tech", "#FF5733", "laptop");
        Category cat2 = Category.create("News", "#33FF57", "newspaper");
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        // When
        List<Category> result = service.getAllCategories();

        // Then
        assertEquals(2, result.size());
        assertEquals("Tech", result.get(0).getName());
        assertEquals("News", result.get(1).getName());
    }

    @Test
    void createCategory_ShouldCreateAndSave() {
        // Given
        String name = "Tech";
        String color = "#FF5733";
        String icon = "laptop";

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            // Simuliere ID-Generierung
            return new Category(CategoryId.of(VALID_UUID), cat.getName(), cat.getColor(), cat.getIcon());
        });

        // When
        Category result = service.createCategory(name, color, icon);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(color, result.getColor());
        assertEquals(icon, result.getIcon());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WithNullIcon_ShouldCreateWithDefaultIcon() {
        // Given
        String name = "General";
        String color = "#333333";
        String icon = null;

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category result = service.createCategory(name, color, icon);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        // Service setzt Default-Icon "label" wenn null
        assertEquals("label", result.getIcon());
    }

    @Test
    void deleteCategory_ShouldCallRepository() {
        // Given
        String categoryId = VALID_UUID;

        // When
        service.deleteCategory(categoryId);

        // Then
        verify(categoryRepository).deleteById(CategoryId.of(categoryId));
    }

    @Test
    void getAllCategories_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(categoryRepository.findAll()).thenReturn(List.of());

        // When
        List<Category> result = service.getAllCategories();

        // Then
        assertTrue(result.isEmpty());
    }
}
