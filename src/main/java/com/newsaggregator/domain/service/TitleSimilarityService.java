package com.newsaggregator.domain.service;

import org.springframework.stereotype.Service;

/**
 * Service zur Prüfung von Titel-Ähnlichkeit für Duplikat-Erkennung.
 * Verwendet normalisierte Levenshtein-Distanz.
 */
@Service
public class TitleSimilarityService {

    private static final double DEFAULT_THRESHOLD = 0.85;

    /**
     * Prüft, ob zwei Titel als Duplikate gelten.
     * @param title1 Erster Titel
     * @param title2 Zweiter Titel
     * @return true, wenn die Titel mindestens threshold ähnlich sind
     */
    public boolean isSimilar(String title1, String title2) {
        return isSimilar(title1, title2, DEFAULT_THRESHOLD);
    }

    /**
     * Prüft, ob zwei Titel als Duplikate gelten mit gegebenem Threshold.
     * @param title1 Erster Titel
     * @param title2 Zweiter Titel
     * @param threshold Minimale Ähnlichkeit (0.0 - 1.0)
     * @return true, wenn die Titel mindestens threshold ähnlich sind
     */
    public boolean isSimilar(String title1, String title2, double threshold) {
        if (title1 == null || title2 == null) return false;
        if (title1.equalsIgnoreCase(title2)) return true;

        String n1 = normalize(title1);
        String n2 = normalize(title2);

        if (n1.equals(n2)) return true;
        if (n1.length() == 0 || n2.length() == 0) return false;

        double similarity = 1.0 - ((double) levenshtein(n1, n2) / Math.max(n1.length(), n2.length()));
        return similarity >= threshold;
    }

    /**
     * Berechnet die Ähnlichkeit zwischen zwei Titeln (0.0 - 1.0).
     */
    public double similarity(String title1, String title2) {
        if (title1 == null || title2 == null) return 0.0;

        String n1 = normalize(title1);
        String n2 = normalize(title2);

        if (n1.equals(n2)) return 1.0;
        if (n1.length() == 0 || n2.length() == 0) return 0.0;

        return 1.0 - ((double) levenshtein(n1, n2) / Math.max(n1.length(), n2.length()));
    }

    private String normalize(String title) {
        return title.toLowerCase()
                    .replaceAll("\\s+", " ")
                    .replaceAll("[^\\p{Alnum}\\s]", "")
                    .trim();
    }

    /**
     * Levenshtein-Distanz (iterativ, O(n*m)).
     */
    private int levenshtein(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) prev[j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[s2.length()];
    }
}
