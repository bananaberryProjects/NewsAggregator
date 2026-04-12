package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.dto.FeedDto;
import com.newsaggregator.application.mapper.FeedMapper;
import com.newsaggregator.application.service.ArticleSearchService;
import com.newsaggregator.domain.port.in.GetAllFeedsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Web Controller für Thymeleaf-Views.
 *
 * <p>Dieser Controller bereitet Daten für die HTML-Oberfläche vor.
 * Er nutzt die gleichen Application Services wie die REST-API.</p>
 */
@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private final GetAllFeedsUseCase getAllFeedsUseCase;
    private final FeedMapper feedMapper;
    private final ArticleSearchService articleSearchService;

    public WebController(GetAllFeedsUseCase getAllFeedsUseCase,
                         FeedMapper feedMapper,
                         ArticleSearchService articleSearchService) {
        this.getAllFeedsUseCase = getAllFeedsUseCase;
        this.feedMapper = feedMapper;
        this.articleSearchService = articleSearchService;
    }

    /**
     * Hauptseite - Zeigt alle Feeds und Artikel.
     */
    @GetMapping("/")
    public String index(Model model) {
        logger.debug("Rufe Index-Seite auf");

        List<FeedDto> feeds = getAllFeedsUseCase.getAllFeeds().stream()
                .map(feedMapper::toDto)
                .collect(Collectors.toList());
        List<ArticleDto> articles = articleSearchService.getAllArticles();

        model.addAttribute("feeds", feeds);
        model.addAttribute("articles", articles);
        model.addAttribute("feedCount", feeds.size());
        model.addAttribute("articleCount", articles.size());

        return "index";
    }

    /**
     * Feed-Verwaltung Seite.
     */
    @GetMapping("/feeds")
    public String feeds(Model model) {
        logger.debug("Rufe Feeds-Seite auf");

        List<FeedDto> feeds = getAllFeedsUseCase.getAllFeeds().stream()
                .map(feedMapper::toDto)
                .collect(Collectors.toList());
        model.addAttribute("feeds", feeds);

        return "feeds";
    }

    /**
     * Artikel-Seite - Zeigt alle Artikel.
     */
    @GetMapping("/articles")
    public String articles(Model model) {
        logger.debug("Rufe Artikel-Seite auf");

        List<ArticleDto> articles = articleSearchService.getAllArticles();
        model.addAttribute("articles", articles);
        model.addAttribute("articleCount", articles.size());

        return "articles";
    }

    /**
     * Suchseite.
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query, Model model) {
        logger.debug("Rufe Suchseite auf mit Query: {}", query);

        if (query != null && !query.trim().isEmpty()) {
            List<ArticleDto> results = articleSearchService.searchArticlesDto(query.trim());
            model.addAttribute("results", results);
            model.addAttribute("query", query);
            model.addAttribute("resultCount", results.size());
        }

        return "search";
    }
}
