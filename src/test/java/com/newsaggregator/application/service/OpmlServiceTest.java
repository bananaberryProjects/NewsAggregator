package com.newsaggregator.application.service;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.out.FeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpmlServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private AddFeedUseCase addFeedUseCase;

    private OpmlService opmlService;

    @BeforeEach
    void setUp() {
        opmlService = new OpmlService(feedRepository, addFeedUseCase);
    }

    @Test
    void exportToOpml_emptyFeedList_generatesValidEmptyOpml() {
        // Given
        when(feedRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        String result = opmlService.exportToOpml();

        // Then
        assertThat(result).contains("<?xml version=");
        assertThat(result).contains("<opml version=\"2.0\">");
        assertThat(result).contains("<title>News Aggregator Feeds</title>");
        assertThat(result).contains("<body>");
        assertThat(result).contains("</body>");
        assertThat(result).contains("</opml>");
        assertThat(result).doesNotContain("<outline");
    }

    @Test
    void exportToOpml_withFeeds_generatesValidOpmlWithOutlines() {
        // Given
        Feed feed1 = Feed.create("Feed 1", "https://example1.com/rss", "Description 1");
        Feed feed2 = Feed.create("Feed 2", "https://example2.com/rss", null);
        List<Feed> feeds = List.of(feed1, feed2);

        when(feedRepository.findAll()).thenReturn(feeds);

        // When
        String result = opmlService.exportToOpml();

        // Then
        assertThat(result).contains("<?xml version=");
        assertThat(result).contains("<opml version=\"2.0\">");
        assertThat(result).contains("<outline");
        assertThat(result).contains("xmlUrl=\"https://example1.com/rss\"");
        assertThat(result).contains("xmlUrl=\"https://example2.com/rss\"");
        assertThat(result).contains("text=\"Feed 1\"");
        assertThat(result).contains("description=\"Description 1\"");
        assertThat(result).contains("</opml>");
    }

    @Test
    void importFromOpml_validOpml_importsFeeds() {
        // Given
        String opmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="2.0">
                    <head>
                        <title>Test Feeds</title>
                    </head>
                    <body>
                        <outline text="Feed 1" title="Feed 1" xmlUrl="https://example1.com/rss" description="Description 1"/>
                        <outline text="Feed 2" title="Feed 2" xmlUrl="https://example2.com/rss"/>
                    </body>
                </opml>
                """;

        when(feedRepository.existsByUrl("https://example1.com/rss")).thenReturn(false);
        when(feedRepository.existsByUrl("https://example2.com/rss")).thenReturn(false);

        Feed importedFeed1 = Feed.create("Feed 1", "https://example1.com/rss", "Description 1");
        Feed importedFeed2 = Feed.create("Feed 2", "https://example2.com/rss", null);
        when(addFeedUseCase.addFeed("Feed 1", "https://example1.com/rss", "Description 1")).thenReturn(importedFeed1);
        when(addFeedUseCase.addFeed("Feed 2", "https://example2.com/rss", null)).thenReturn(importedFeed2);

        // When
        List<Feed> result = opmlService.importFromOpml(opmlContent);

        // Then
        assertThat(result).hasSize(2);
        verify(addFeedUseCase, times(1)).addFeed("Feed 1", "https://example1.com/rss", "Description 1");
        verify(addFeedUseCase, times(1)).addFeed("Feed 2", "https://example2.com/rss", null);
    }

    @Test
    void importFromOpml_duplicateFeeds_skipsDuplicates() {
        // Given
        String opmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="2.0">
                    <head>
                        <title>Test Feeds</title>
                    </head>
                    <body>
                        <outline text="Existing Feed" title="Existing Feed" xmlUrl="https://existing.com/rss"/>
                        <outline text="New Feed" title="New Feed" xmlUrl="https://new.com/rss"/>
                    </body>
                </opml>
                """;

        when(feedRepository.existsByUrl("https://existing.com/rss")).thenReturn(true);
        when(feedRepository.existsByUrl("https://new.com/rss")).thenReturn(false);

        Feed importedFeed = Feed.create("New Feed", "https://new.com/rss", null);
        when(addFeedUseCase.addFeed("New Feed", "https://new.com/rss", null)).thenReturn(importedFeed);

        // When
        List<Feed> result = opmlService.importFromOpml(opmlContent);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("New Feed");
        verify(addFeedUseCase, never()).addFeed(eq("Existing Feed"), anyString(), any());
        verify(addFeedUseCase, times(1)).addFeed("New Feed", "https://new.com/rss", null);
    }

    @Test
    void importFromOpml_invalidXml_throwsException() {
        // Given
        String invalidOpml = "This is not valid XML < unclosed tag";

        // When / Then
        assertThatThrownBy(() -> opmlService.importFromOpml(invalidOpml))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Fehler beim Importieren der OPML-Datei");
    }

    @Test
    void importFromOpml_outlineWithoutXmlUrl_skipsNonFeedOutlines() {
        // Given
        String opmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="2.0">
                    <head>
                        <title>Test Feeds</title>
                    </head>
                    <body>
                        <outline text="Category" title="Category">
                            <outline text="Subcategory" title="Subcategory"/>
                        </outline>
                        <outline text="Real Feed" title="Real Feed" xmlUrl="https://example.com/rss"/>
                    </body>
                </opml>
                """;

        when(feedRepository.existsByUrl("https://example.com/rss")).thenReturn(false);

        Feed importedFeed = Feed.create("Real Feed", "https://example.com/rss", null);
        when(addFeedUseCase.addFeed("Real Feed", "https://example.com/rss", null)).thenReturn(importedFeed);

        // When
        List<Feed> result = opmlService.importFromOpml(opmlContent);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Real Feed");
        verify(addFeedUseCase, times(1)).addFeed(anyString(), anyString(), any());
    }

    @Test
    void importFromOpml_emptyName_usesFallback() {
        // Given
        String opmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="2.0">
                    <head>
                        <title>Test Feeds</title>
                    </head>
                    <body>
                        <outline title="Fallback Title" xmlUrl="https://example.com/rss"/>
                        <outline xmlUrl="https://example2.com/rss"/>
                    </body>
                </opml>
                """;

        when(feedRepository.existsByUrl("https://example.com/rss")).thenReturn(false);
        when(feedRepository.existsByUrl("https://example2.com/rss")).thenReturn(false);

        Feed importedFeed1 = Feed.create("Fallback Title", "https://example.com/rss", null);
        Feed importedFeed2 = Feed.create("Unnamed Feed", "https://example2.com/rss", null);
        when(addFeedUseCase.addFeed("Fallback Title", "https://example.com/rss", null)).thenReturn(importedFeed1);
        when(addFeedUseCase.addFeed("Unnamed Feed", "https://example2.com/rss", null)).thenReturn(importedFeed2);

        // When
        List<Feed> result = opmlService.importFromOpml(opmlContent);

        // Then
        assertThat(result).hasSize(2);
        verify(addFeedUseCase).addFeed("Fallback Title", "https://example.com/rss", null);
        verify(addFeedUseCase).addFeed("Unnamed Feed", "https://example2.com/rss", null);
    }
}
