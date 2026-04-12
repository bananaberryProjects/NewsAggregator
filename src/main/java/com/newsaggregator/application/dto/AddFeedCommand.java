package com.newsaggregator.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command-DTO für das Hinzufügen eines neuen Feeds.
 *
 * <p>Dieses DTO enthält alle notwendigen Daten, um einen neuen Feed zu erstellen.
 * Es wird typischerweise aus einem HTTP-Request erstellt.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFeedCommand {

    private String name;
    private String url;
    private String description;

}
