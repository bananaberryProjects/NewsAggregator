package com.newsaggregator.domain.port.out;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;

import java.util.List;

/**
 * Outgoing Port für das Lesen von RSS-Feeds.
 *
 * Dieses Interface abstrahiert das externe RSS-Lesen.
 * Die tatsächliche Implementierung (z.B. mit Rome oder einem anderen Parser)
 * liegt in der Infrastruktur.
 */
public interface RssFeedReader {

    /**
     * Liest einen RSS/Atom Feed von der angegebenen URL.
     *
     * @param feed Der Feed, der gelesen werden soll (enthält die URL)
     * @return Liste der gefundenen Artikel
     * @throws RssReadException wenn das Feed nicht gelesen werden kann
     */
    List<Article> readFeed(Feed feed) throws RssReadException;

    /**
     * Exception für RSS-Lesefehler.
     */
    class RssReadException extends Exception {
        public RssReadException(String message) {
            super(message);
        }

        public RssReadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
