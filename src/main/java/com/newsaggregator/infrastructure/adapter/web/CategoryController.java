package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.CategoryDto;
import com.newsaggregator.application.dto.UpdateCategoryCommand;
import com.newsaggregator.application.service.CategoryService;
import com.newsaggregator.application.service.UpdateCategoryService;
import com.newsaggregator.domain.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CategoryController {
    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;
    private final UpdateCategoryService updateCategoryService;

    public CategoryController(CategoryService categoryService, UpdateCategoryService updateCategoryService) {
        this.categoryService = categoryService;
        this.updateCategoryService = updateCategoryService;
    }

    private CategoryDto toDto(Category category) {
        return new CategoryDto(
            category.getId().getValue().toString(),
            category.getName(),
            category.getColor(),
            category.getIcon()
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        log.info("GET /api/categories aufgerufen");
        List<CategoryDto> dtos = categoryService.getAllCategories().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String color = request.getOrDefault("color", "#667eea");
        String icon = request.getOrDefault("icon", "label");
        log.info("POST /api/categories aufgerufen: {}", name);
        Category category = categoryService.createCategory(name, color, icon);
        return ResponseEntity.ok(toDto(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        log.info("DELETE /api/categories/{} aufgerufen", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryCommand command) {
        log.info("PUT /api/categories/{} aufgerufen: {}", id, command.getName());
        try {
            Category category = updateCategoryService.updateCategory(
                id, command.getName(), command.getColor(), command.getIcon());
            return ResponseEntity.ok(toDto(category));
        } catch (IllegalArgumentException e) {
            log.warn("Fehler beim Aktualisieren der Kategorie: {}", e.getMessage());
            if (e.getMessage().contains("nicht gefunden")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
}
