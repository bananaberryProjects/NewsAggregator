package com.newsaggregator.application.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.domain.port.out.CategoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Test für UpdateCategoryService.
 *
 * <p>Testet die Geschäftslogik für das Aktualisieren von Kategorien.</p>
 */
@ExtendWith(MockitoExtension.class)
class UpdateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private UpdateCategoryService service;

    private static final String VALID_UUID = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        service = new UpdateCategoryService(categoryRepository);
    }

    @Test
    void updateCategory_ShouldUpdateAndSave() {
        // Given
        Category existingCategory = new Category(
                CategoryId.of(VALID_UUID),
                "Tech",
                "#FF5733",
                "laptop"
        );

        when(categoryRepository.findById(CategoryId.of(VALID_UUID)))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category result = service.updateCategory(VALID_UUID, "Technology", "#3366FF", "desktop");

        // Then
        assertThat(result)
                .isNotNull()
                .extracting(Category::getName, Category::getColor, Category::getIcon)
                .containsExactly("Technology", "#3366FF", "desktop");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldTrimName() {
        // Given
        Category existingCategory = new Category(
                CategoryId.of(VALID_UUID),
                "Tech",
                "#FF5733",
                "laptop"
        );

        when(categoryRepository.findById(CategoryId.of(VALID_UUID)))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category result = service.updateCategory(VALID_UUID, "  Technology  ", "#3366FF", "desktop");

        // Then
        assertThat(result.getName()).isEqualTo("Technology");
    }

    @Test
    void updateCategory_WithNullName_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> service.updateCategory(VALID_UUID, null, "#3366FF", "desktop"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kategorie-Name darf nicht leer sein");
    }

    @Test
    void updateCategory_WithEmptyName_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> service.updateCategory(VALID_UUID, "   ", "#3366FF", "desktop"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kategorie-Name darf nicht leer sein");
    }

    @Test
    void updateCategory_WithNonExistingId_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(CategoryId.of(VALID_UUID)))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.updateCategory(VALID_UUID, "Technology", "#3366FF", "desktop"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kategorie mit ID " + VALID_UUID + " nicht gefunden");
    }

    @Test
    void updateCategory_WithNullColor_ShouldKeepExistingColor() {
        // Given
        Category existingCategory = new Category(
                CategoryId.of(VALID_UUID),
                "Tech",
                "#FF5733",
                "laptop"
        );

        when(categoryRepository.findById(CategoryId.of(VALID_UUID)))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category result = service.updateCategory(VALID_UUID, "Technology", null, "desktop");

        // Then
        assertThat(result.getColor()).isEqualTo("#FF5733");
    }

    @Test
    void updateCategory_WithNullIcon_ShouldKeepExistingIcon() {
        // Given
        Category existingCategory = new Category(
                CategoryId.of(VALID_UUID),
                "Tech",
                "#FF5733",
                "laptop"
        );

        when(categoryRepository.findById(CategoryId.of(VALID_UUID)))
                .thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Category result = service.updateCategory(VALID_UUID, "Technology", "#3366FF", null);

        // Then
        assertThat(result.getIcon()).isEqualTo("laptop");
    }
}
