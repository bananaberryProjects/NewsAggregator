package com.newsaggregator.domain.port.out;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;

import java.util.List;
import java.util.Optional;

/**
 * Outgoing Port für Feed-Persistenz.
 * 
 * Dieses Interface definiert, was die Domain von der Persistenzschicht erwartet.
 * Die tatsächliche Implementierung liegt in der Infrastruktur.
 */
public interface FeedRepository {

    /**
     * Speichert einen Feed.
     * Erstellt einen neuen oder aktualisiert einen vorhandenen Feed.
     *
     * @param feed Der zu speichernde Feed
     * @return Der gespeicherte Feed (mit ID, falls neu)
     */
    Feed save(Feed feed);

    /**
     * Sucht einen Feed anhand seiner ID.
     *
     * @param id Die Feed-ID
     * @return Optional mit dem Feed, falls gefunden
     */
    Optional<Feed> findById(FeedId id);

    /**
     * Gibt alle Feeds zurück.
     *
     * @return Liste aller Feeds
     */
    List<Feed> findAll();

    /**
     * Löscht einen Feed anhand seiner ID.
     *
     * @param id Die Feed-ID
     */
    void deleteById(FeedId id);

    /**
     * Prüft, ob ein Feed mit dieser URL existiert.
     *
     * @param url Die zu prüfende URL
     * @return true, falls ein Feed mit dieser URL existiert
     */
    boolean existsByUrl(String url);

    /**
     * Sucht einen Feed anhand seiner URL.
     *
     * @param url Die URL des Feeds
     * @return Optional mit dem Feed, falls gefunden
     */
    Optional<Feed> findByUrl(String url);
}
