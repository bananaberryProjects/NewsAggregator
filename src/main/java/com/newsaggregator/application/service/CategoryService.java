package com.newsaggregator.application.service;

import com.newsaggregator.application.port.out.CategoryRepository;
import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(String name, String color, String icon) {
        log.info("Erstelle neue Kategorie: {}", name);
        Category category = Category.create(name, color, icon);
        return categoryRepository.save(category);
    }

    public void deleteCategory(String id) {
        log.info("Lösche Kategorie: {}", id);
        categoryRepository.deleteById(CategoryId.of(id));
    }
}
