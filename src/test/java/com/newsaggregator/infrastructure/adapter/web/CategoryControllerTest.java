package com.newsaggregator.infrastructure.adapter.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.service.CategoryService;
import com.newsaggregator.domain.model.Category;

/**
 * Unit-Test für CategoryController.
 *
 * <p>Testet die REST-Endpunkte für Kategorie-Operationen.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        // Given
        Category category1 = Category.create("Technology", "#3b82f6", "computer");
        Category category2 = Category.create("Science", "#10b981", "science");

        when(categoryService.getAllCategories()).thenReturn(List.of(category1, category2));

        // When / Then
        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Technology"))
                .andExpect(jsonPath("$[0].color").value("#3b82f6"))
                .andExpect(jsonPath("$[0].icon").value("computer"))
                .andExpect(jsonPath("$[1].name").value("Science"))
                .andExpect(jsonPath("$[1].color").value("#10b981"))
                .andExpect(jsonPath("$[1].icon").value("science"));
    }

    @Test
    void getAllCategories_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(categoryService.getAllCategories()).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createCategory_WithAllFields_ShouldReturnCreatedCategory() throws Exception {
        // Given
        String name = "Sports";
        String color = "#f59e0b";
        String icon = "sports";
        Category createdCategory = Category.create(name, color, icon);

        when(categoryService.createCategory(name, color, icon)).thenReturn(createdCategory);

        Map<String, String> request = Map.of(
                "name", name,
                "color", color,
                "icon", icon
        );

        // When / Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sports"))
                .andExpect(jsonPath("$.color").value("#f59e0b"))
                .andExpect(jsonPath("$.icon").value("sports"));
    }

    @Test
    void createCategory_WithDefaultValues_ShouldReturnCategoryWithDefaults() throws Exception {
        // Given
        String name = "General";
        // Default values: color = "#667eea", icon = "label"
        Category createdCategory = Category.create(name, null, null);

        when(categoryService.createCategory(name, "#667eea", "label")).thenReturn(createdCategory);

        Map<String, String> request = Map.of("name", name);

        // When / Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("General"))
                .andExpect(jsonPath("$.color").value("#667eea"))
                .andExpect(jsonPath("$.icon").value("label"));
    }

    @Test
    void createCategory_WithPartialDefaults_ShouldReturnCategoryWithMixedValues() throws Exception {
        // Given
        String name = "Entertainment";
        String color = "#ec4899";
        String defaultIcon = "label";
        Category createdCategory = Category.create(name, color, null);

        when(categoryService.createCategory(name, color, defaultIcon)).thenReturn(createdCategory);

        Map<String, String> request = Map.of(
                "name", name,
                "color", color
        );

        // When / Then
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Entertainment"))
                .andExpect(jsonPath("$.color").value("#ec4899"))
                .andExpect(jsonPath("$.icon").value("label"));
    }

    @Test
    void deleteCategory_WithExistingId_ShouldReturnOk() throws Exception {
        // Given
        String categoryId = UUID.randomUUID().toString();
        doNothing().when(categoryService).deleteCategory(categoryId);

        // When / Then
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}