package com.newsaggregator.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Command-DTO für das Aktualisieren eines bestehenden Feeds.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedCommand {

    private String name;
    private String url;
    private String description;
    private Boolean extractContent;
    private List<String> blockedKeywords;

}
