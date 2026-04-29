package com.newsaggregator.domain.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TitleSimilarityServiceTest {

    private final TitleSimilarityService service = new TitleSimilarityService();

    @Test
    void identicalTitles_shouldBeSimilar() {
        assertTrue(service.isSimilar("Bitcoin Preis steigt", "Bitcoin Preis steigt"));
    }

    @Test
    void caseInsensitive_shouldBeSimilar() {
        assertTrue(service.isSimilar("BITCOIN PREIS STEIGT", "bitcoin preis steigt"));
    }

    @Test
    void punctuationNormalized_shouldBeSimilar() {
        assertTrue(service.isSimilar("Bitcoin: Preis steigt!", "Bitcoin Preis steigt"));
    }

    @Test
    void completelyDifferent_shouldNotBeSimilar() {
        assertFalse(service.isSimilar("Bitcoin Preis", "Ethereum Kurs fällt"));
    }

    @Test
    void similarityScore_shouldBeOneForIdentical() {
        assertEquals(1.0, service.similarity("Bitcoin", "Bitcoin"), 0.001);
    }

    @Test
    void similarityScore_shouldBeZeroForDifferent() {
        assertEquals(0.0, service.similarity("aaa", "bbb"), 0.001);
    }

    @Test
    void emptyTitles_shouldNotBeSimilar() {
        assertFalse(service.isSimilar("", "Test"));
        assertFalse(service.isSimilar(null, "Test"));
    }

}
