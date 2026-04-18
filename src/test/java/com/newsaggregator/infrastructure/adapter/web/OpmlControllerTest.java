package com.newsaggregator.infrastructure.adapter.web;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.out.FeedRepository;

/**
 * Web-Test fuer OpmlController.
 *
 * Testet den REST Controller fuer OPML Import/Export mit MockMvc.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu
 * vermeiden.
 */
@ExtendWith(MockitoExtension.class)
class OpmlControllerTest {

        private MockMvc mockMvc;

        @Mock
        private FeedRepository feedRepository;

        @Mock
        private AddFeedUseCase addFeedUseCase;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                OpmlController opmlController = new OpmlController(feedRepository, addFeedUseCase);
                mockMvc = MockMvcBuilders.standaloneSetup(opmlController).build();
        }

        private Feed createTestFeed(Long id, String name, String url, String description) {
                return Feed.of(
                                FeedId.of(id),
                                name,
                                url,
                                description,
                                LocalDateTime.now(),
                                null,
                                FeedStatus.ACTIVE);
        }

        @Test
        void importOpml_ShouldImportFeedsSuccessfully() throws Exception {
                // Given
                String opmlContent = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <opml version="2.0">
                                         <head><title>Test Feeds</title></head>
                                         <body>
                                             <outline text="Feed 1" title="Feed 1" type="rss" xmlUrl="https://example.com/feed1"/>
                                             <outline text="Feed 2" title="Feed 2" type="rss" xmlUrl="https://example.com/feed2"/>
                                         </body>
                                     </opml>""";

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "feeds.opml",
                                "application/xml",
                                opmlContent.getBytes(StandardCharsets.UTF_8));

                when(addFeedUseCase.addFeed(eq("Feed 1"), eq("https://example.com/feed1"), any()))
                                .thenReturn(createTestFeed(1L, "Feed 1", "https://example.com/feed1", null));
                when(addFeedUseCase.addFeed(eq("Feed 2"), eq("https://example.com/feed2"), any()))
                                .thenReturn(createTestFeed(2L, "Feed 2", "https://example.com/feed2", null));

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(file))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentAsString()).isEqualTo("Erfolgreich 2 Feeds importiert");

                verify(addFeedUseCase).addFeed(eq("Feed 1"), eq("https://example.com/feed1"), eq(null));
                verify(addFeedUseCase).addFeed(eq("Feed 2"), eq("https://example.com/feed2"), eq(null));
        }

        @Test
        void importOpml_ShouldUseTextWhenTitleIsEmpty() throws Exception {
                // Given
                String opmlContent = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <opml version="2.0">
                                         <head><title>Test Feeds</title></head>
                                         <body>
                                             <outline text="Feed Name" type="rss" xmlUrl="https://example.com/feed"/>
                                         </body>
                                     </opml>""";

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "feeds.opml",
                                "application/xml",
                                opmlContent.getBytes(StandardCharsets.UTF_8));

                when(addFeedUseCase.addFeed(eq("Feed Name"), eq("https://example.com/feed"), any()))
                                .thenReturn(createTestFeed(1L, "Feed Name", "https://example.com/feed", null));

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(file))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentAsString()).isEqualTo("Erfolgreich 1 Feeds importiert");
        }

        @Test
        void importOpml_ShouldUseDefaultNameWhenTitleAndTextAreEmpty() throws Exception {
                // Given
                String opmlContent = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <opml version="2.0">
                                         <head><title>Test Feeds</title></head>
                                         <body>
                                             <outline type="rss" xmlUrl="https://example.com/feed"/>
                                         </body>
                                     </opml>""";

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "feeds.opml",
                                "application/xml",
                                opmlContent.getBytes(StandardCharsets.UTF_8));

                when(addFeedUseCase.addFeed(eq("Unnamed Feed"), eq("https://example.com/feed"), any()))
                                .thenReturn(createTestFeed(1L, "Unnamed Feed", "https://example.com/feed", null));

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(file))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentAsString()).isEqualTo("Erfolgreich 1 Feeds importiert");
        }

        @Test
        void importOpml_ShouldSkipFeedsWithoutXmlUrl() throws Exception {
                // Given
                String opmlContent = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <opml version="2.0">
                                         <head><title>Test Feeds</title></head>
                                         <body>
                                             <outline text="Category" type="folder"/>
                                             <outline text="Valid Feed" title="Valid Feed" type="rss" xmlUrl="https://example.com/feed"/>
                                         </body>
                                     </opml>""";

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "feeds.opml",
                                "application/xml",
                                opmlContent.getBytes(StandardCharsets.UTF_8));

                when(addFeedUseCase.addFeed(eq("Valid Feed"), eq("https://example.com/feed"), any()))
                                .thenReturn(createTestFeed(1L, "Valid Feed", "https://example.com/feed", null));

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(file))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentAsString()).isEqualTo("Erfolgreich 1 Feeds importiert");

                verify(addFeedUseCase, times(1)).addFeed(any(), any(), any());
        }

        @Test
        void importOpml_ShouldSkipDuplicateFeeds() throws Exception {
                // Given
                String opmlContent = """
                                     <?xml version="1.0" encoding="UTF-8"?>
                                     <opml version="2.0">
                                         <head><title>Test Feeds</title></head>
                                         <body>
                                             <outline text="Feed 1" title="Feed 1" type="rss" xmlUrl="https://example.com/feed1"/>
                                         </body>
                                     </opml>""";

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "feeds.opml",
                                "application/xml",
                                opmlContent.getBytes(StandardCharsets.UTF_8));

                when(addFeedUseCase.addFeed(any(), any(), any()))
                                .thenThrow(new IllegalArgumentException("Feed already exists"));

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(file))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentAsString()).isEqualTo("Erfolgreich 0 Feeds importiert");
        }

        @Test
        void importOpml_ShouldReturnBadRequest_WhenFileIsEmpty() throws Exception {
                // Given
                MockMultipartFile emptyFile = new MockMultipartFile(
                                "file",
                                "empty.opml",
                                "application/xml",
                                new byte[0]);

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(emptyFile))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(400);
                assertThat(result.getResponse().getContentAsString()).isEqualTo("Datei ist leer");
        }

        @Test
        void importOpml_ShouldReturnError_WhenInvalidXml() throws Exception {
                // Given
                MockMultipartFile invalidFile = new MockMultipartFile(
                                "file",
                                "invalid.opml",
                                "application/xml",
                                "not valid xml content".getBytes(StandardCharsets.UTF_8));

                // When
                MvcResult result = mockMvc.perform(multipart("/api/opml/import").file(invalidFile))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(500);
        }

        @Test
        void exportOpml_ShouldExportFeedsSuccessfully() throws Exception {
                // Given
                Feed feed1 = createTestFeed(1L, "Test Feed 1", "https://example.com/feed1", "Description 1");
                Feed feed2 = createTestFeed(2L, "Test Feed 2", "https://example.com/feed2", "Description 2");

                when(feedRepository.findAll()).thenReturn(List.of(feed1, feed2));

                // When
                MvcResult result = mockMvc.perform(get("/api/opml/export"))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_XML_VALUE);
                assertThat(result.getResponse().getHeader("Content-Disposition")).contains("filename=\"feeds.opml\"");

                String content = result.getResponse().getContentAsString();
                assertThat(content).contains("<opml version=\"2.0\">");
                assertThat(content).contains("<title>News Aggregator Feeds</title>");
                assertThat(content).contains("text=\"Test Feed 1\"");
                assertThat(content).contains("xmlUrl=\"https://example.com/feed1\"");
                assertThat(content).contains("description=\"Description 1\"");
                assertThat(content).contains("text=\"Test Feed 2\"");
                assertThat(content).contains("xmlUrl=\"https://example.com/feed2\"");
        }

        @Test
        void exportOpml_ShouldExportEmptyOpml_WhenNoFeeds() throws Exception {
                // Given
                when(feedRepository.findAll()).thenReturn(List.of());

                // When
                MvcResult result = mockMvc.perform(get("/api/opml/export"))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_XML_VALUE);

                String content = result.getResponse().getContentAsString();
                assertThat(content).contains("<opml version=\"2.0\">");
                assertThat(content).contains("<title>News Aggregator Feeds</title>");
                assertThat(content).contains("<body/>");
        }

        @Test
        void exportOpml_ShouldOmitDescription_WhenNull() throws Exception {
                // Given
                Feed feed = createTestFeed(1L, "Test Feed", "https://example.com/feed", null);

                when(feedRepository.findAll()).thenReturn(List.of(feed));

                // When
                MvcResult result = mockMvc.perform(get("/api/opml/export"))
                                .andReturn();

                // Then
                assertThat(result.getResponse().getStatus()).isEqualTo(200);
                assertThat(result.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_XML_VALUE);

                String content = result.getResponse().getContentAsString();
                assertThat(content).contains("text=\"Test Feed\"");
                assertThat(content).contains("xmlUrl=\"https://example.com/feed\"");
                assertThat(content).doesNotContain("description");
        }
}
