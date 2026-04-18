package com.newsaggregator.infrastructure.adapter.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.dto.CategoryDto;
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
    @SuppressWarnings("unused")
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<CategoryDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertThat(responseBody).hasSize(2);
        assertThat(responseBody.get(0).name()).isEqualTo("Technology");
        assertThat(responseBody.get(0).color()).isEqualTo("#3b82f6");
        assertThat(responseBody.get(0).icon()).isEqualTo("computer");
        assertThat(responseBody.get(1).name()).isEqualTo("Science");
        assertThat(responseBody.get(1).color()).isEqualTo("#10b981");
        assertThat(responseBody.get(1).icon()).isEqualTo("science");
    }

    @Test
    void getAllCategories_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(categoryService.getAllCategories()).thenReturn(List.of());

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<CategoryDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {});
        assertThat(responseBody).isEmpty();
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        CategoryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryDto.class);
        assertThat(responseBody.name()).isEqualTo("Sports");
        assertThat(responseBody.color()).isEqualTo("#f59e0b");
        assertThat(responseBody.icon()).isEqualTo("sports");
    }

    @Test
    void createCategory_WithDefaultValues_ShouldReturnCategoryWithDefaults() throws Exception {
        // Given
        String name = "General";
        // Default values: color = "#667eea", icon = "label"
        Category createdCategory = Category.create(name, null, null);

        when(categoryService.createCategory(name, "#667eea", "label")).thenReturn(createdCategory);

        Map<String, String> request = Map.of("name", name);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        CategoryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryDto.class);
        assertThat(responseBody.name()).isEqualTo("General");
        assertThat(responseBody.color()).isEqualTo("#667eea");
        assertThat(responseBody.icon()).isEqualTo("label");
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        CategoryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CategoryDto.class);
        assertThat(responseBody.name()).isEqualTo("Entertainment");
        assertThat(responseBody.color()).isEqualTo("#ec4899");
        assertThat(responseBody.icon()).isEqualTo("label");
    }

    @Test
    void deleteCategory_WithExistingId_ShouldReturnOk() throws Exception {
        // Given
        String categoryId = UUID.randomUUID().toString();
        doNothing().when(categoryService).deleteCategory(categoryId);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(delete("/api/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
