package com.newsaggregator.application.service;

import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.domain.port.in.UpdateCategoryUseCase;
import com.newsaggregator.domain.port.out.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application Service für das Aktualisieren von Kategorien.
 */
@Service
@Transactional
public class UpdateCategoryService implements UpdateCategoryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCategoryService.class);

    private final CategoryRepository categoryRepository;

    public UpdateCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category updateCategory(String id, String name, String color, String icon) {
        logger.info("Aktualisiere Kategorie mit ID {}: name={}, color={}, icon={}", id, name, color);

        // Validierung
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Kategorie-Name darf nicht leer sein");
        }

        // Kategorie laden
        Optional<Category> existingCategory = categoryRepository.findById(CategoryId.of(id));
        if (existingCategory.isEmpty()) {
            throw new IllegalArgumentException("Kategorie mit ID " + id + " nicht gefunden");
        }

        Category category = existingCategory.get();
        category.update(name.trim(), color, icon);

        Category updatedCategory = categoryRepository.save(category);
        logger.info("Kategorie {} erfolgreich aktualisiert", id);

        return updatedCategory;
    }
}
