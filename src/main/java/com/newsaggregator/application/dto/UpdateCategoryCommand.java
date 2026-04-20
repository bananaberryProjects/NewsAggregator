package com.newsaggregator.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command-DTO für das Aktualisieren einer bestehenden Kategorie.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryCommand {

    private String name;
    private String color;
    private String icon;

}
