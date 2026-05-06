package com.newsaggregator.application.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Text-Analyse für Trending-Topics mit Apache Lucene.
 *
 * Nutzt {@link GermanAnalyzer} für:
 * - Deutsche Tokenisierung (Whitespace + StandardTokenizer)
 * - Integrierte deutsche Stopword-Liste (~200 Wörter)
 * - Deutsche Lemmatisierung/Stemming (Snowball GermanStemmer)
 *
 * Erweitert mit {@link ShingleFilter} für Bigram-Erkennung:
 * - "Bitcoin ETF" wird als einzelner Begriff "bitcoin etf" extrahiert
 * - Kombination aus Unigrams + Bigrams liefert sowohl Einzelwörter als auch Phrasen
 */
@Component
public class TrendingTextAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TrendingTextAnalyzer.class);

    // Zusätzliche Domain-Noise-Wörter die Lucene's GermanAnalyzer nicht abdeckt
    private static final Set<String> EXTRA_STOPWORDS = Set.of(
        "neu", "neue", "neuer", "neues", "neuen",
        "alte", "alter", "altes", "alten",
        "groß", "große", "großer", "großes", "großen",
        "klein", "kleine", "kleiner", "kleines", "kleinen",
        "update", "updates", "version", "v", "nr", "nummer",
        "bericht", "berichte", "berichten", "meldung", "meldungen",
        "analyse", "analysen", "kommentar", "kommentare",
        "interview", "interviews", "studie", "studien",
        "meinung", "meinungen", "kolumne", "kolumnen",
        "news", "new", "breaking", "exclusive", "exklusiv",
        "neueste", "aktuell", "aktuelle", "aktuellen",
        "jetzt", "sofort", "eben", "gerade", "bald",
        "hier", "dort", "wo", "wann", "warum",
        "weitere", "weiterer", "weiteres", "weiteren",
        "verschiedene", "verschiedener", "verschiedenes", "verschiedenen",
        "andere", "anderer", "anderes", "anderen",
        "alle", "aller", "alles", "allen",
        "viele", "vieler", "vieles", "vielen",
        "wenige", "weniger", "weniges", "wenigen",
        "mehrere", "mehrerer", "mehreres", "mehreren",
        "einige", "einiger", "einiges", "einigen",
        "meiste", "meisten", "meistes",
        "beste", "besten", "bestes",
        "schlechteste", "schlechtesten", "schlechtestes",
        "erste", "ersten", "erster", "erstes",
        "letzte", "letzten", "letzter", "letztes",
        "zweite", "zweiten", "zweiter", "zweites",
        "dritte", "dritten", "dritter", "drittes",
        "tag", "tage", "woche", "wochen", "monat", "monate", "monaten",
        "jahr", "jahre", "jahren", "jahres", "jahrs",
        "heute", "gestern", "morgen", "vorgestern", "übermorgen",
        "nacht", "nächte", "mittag", "mittags", "abend", "abends", "morgens",
        "früh", "spät", "früher", "später", "früheste", "späteste",
        "damals", "einst", "neuerdings", "neulich",
        "immer", "oft", "häufig", "selten", "manchmal",
        "nie", "niemals", "immerhin", "wenigstens", "mindestens",
        "fast", "kaum", "schwerlich", "baldigst",
        "ca", "circa", "etwa", "ungefähr", "maximal",
        "prozent", "prozente", "anti", "pro contra",
        "pro", "kontra", "vs", "versus", "gegen",
        "plus", "minus", "mal", "durch", "geteilt",
        "stück", "stücke", "teile", "teil", "einheit", "einheiten",
        "milliarde", "milliarden", "million", "millionen", "tausend", "hundert",
        // HTML/CSS Reste (falls durchsickern)
        "div", "span", "script", "style", "img", "width", "height", "class",
        "src", "href", "alt", "px", "em", "rem", "com", "de", "org", "net",
        "www", "http", "https", "html", "body", "head", "title", "meta", "link"
    );

    private final Analyzer germanAnalyzer;

    public TrendingTextAnalyzer() {
        // GermanAnalyzer mit erweiterter Stopword-Liste
        this.germanAnalyzer = new GermanAnalyzer(
            GermanAnalyzer.getDefaultStopSet(),
            GermanAnalyzer.getDefaultStopSet()  // beide Sets identisch, wir filtern EXTRA separat
        );
    }

    /**
     * Extrahiert Terms aus einem Text mit Unigram + Bigram Tokenisierung.
     *
     * @param text Rohtext (Titel + Description)
     * @return Liste von (term, isBigram) Paaren
     */
    public List<TermToken> extractTerms(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // 0. HTML + URLs entfernen BEVOR Tokenisierung
        String cleaned = cleanText(text);
        if (cleaned.isBlank()) {
            return List.of();
        }

        List<TermToken> tokens = new ArrayList<>();

        try {
            // 1. GermanAnalyzer Tokenisierung (Stopwords + Stemming)
            List<String> unigrams = tokenizeWithGermanAnalyzer(cleaned);

            // 2. Bigram-Erzeugung aus den gefilterten Unigrams
            List<String> bigrams = generateBigrams(unigrams);

            // 3. Kombiniere: Bigrams zuerst (höhere Relevanz), dann Unigrams
            for (String bigram : bigrams) {
                tokens.add(new TermToken(bigram, true));
            }
            for (String unigram : unigrams) {
                tokens.add(new TermToken(unigram, false));
            }

        } catch (IOException e) {
            log.warn("Lucene Analyse fehlgeschlagen für Text: {}", text.substring(0, Math.min(50, text.length())));
        }

        return tokens;
    }

    /**
     * Bereinigt Rohtext von HTML-Markup, URLs und Entities.
     * Muss vor der Lucene-Analyse passieren.
     */
    private String cleanText(String raw) {
        if (raw == null) {
            return "";
        }

        String text = raw;

        // 1. HTML-Tags entfernen: <div class="foo"> → " "
        text = text.replaceAll("<[^>]+>", " ");

        // 2. URLs entfernen: https://example.com/path?x=1 → " "
        text = text.replaceAll("https?://\\S+", " ");

        // 3. HTML Entities entfernen: &nbsp; &amp; &lt; → " "
        text = text.replaceAll("&\\w+;", " ");
        text = text.replaceAll("&#[0-9]+;", " ");

        // 4. Numerische Token (reine Zahlen) entfernen — "2024", "42" sind kein Thema
        text = text.replaceAll("\\b\\d+\\b", " ");

        // 5. Redundante Whitespace normalisieren
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Tokenisiert Text mit GermanAnalyzer (Stopword-Filter + Stemming).
     */
    private List<String> tokenizeWithGermanAnalyzer(String text) throws IOException {
        List<String> tokens = new ArrayList<>();

        try (TokenStream stream = germanAnalyzer.tokenStream("content", text)) {
            stream.reset();
            CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);

            while (stream.incrementToken()) {
                String term = termAttr.toString().trim();
                // GermanAnalyzer stemmt bereits — wir prüfen noch Länge + extra Stopwords
                if (term.length() >= 3 && !EXTRA_STOPWORDS.contains(term)) {
                    tokens.add(term);
                }
            }
            stream.end();
        }

        return tokens;
    }

    /**
     * Erzeugt Bigrams aus einer Liste von Unigrams.
     * "bitcoin etf zulassung" → ["bitcoin etf", "etf zulassung"]
     */
    private List<String> generateBigrams(List<String> unigrams) {
        List<String> bigrams = new ArrayList<>();
        for (int i = 0; i < unigrams.size() - 1; i++) {
            String bigram = unigrams.get(i) + " " + unigrams.get(i + 1);
            bigrams.add(bigram);
        }
        return bigrams;
    }

    /**
     * Nutzt ShingleFilter für Lucene-basierte Bigram-Tokenisierung.
     * Alternative zu manueller Bigram-Erzeugung — nutzt Lucene's Offsets.
     */
    public List<String> extractShingles(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String cleaned = cleanText(text);
        if (cleaned.isBlank()) {
            return List.of();
        }

        List<String> shingles = new ArrayList<>();

        try {
            try (TokenStream stream = germanAnalyzer.tokenStream("content", cleaned)) {
                // ShingleFilter: max_shingle_size=2 → Unigrams + Bigrams
                ShingleFilter shingleFilter = new ShingleFilter(stream, 2, 2);
                shingleFilter.setOutputUnigrams(false); // nur Bigrams

                shingleFilter.reset();
                CharTermAttribute termAttr = shingleFilter.getAttribute(CharTermAttribute.class);

                while (shingleFilter.incrementToken()) {
                    String term = termAttr.toString().trim();
                    if (term.length() >= 5 && term.contains(" ")) { // nur echte Bigrams
                        shingles.add(term);
                    }
                }
                shingleFilter.end();
            }
        } catch (IOException e) {
            log.warn("Shingle-Analyse fehlgeschlagen: {}", e.getMessage());
        }

        return shingles;
    }

    // --- Record-like DTO ---

    public static class TermToken {
        public final String term;
        public final boolean isBigram;
        public final boolean isNoise;

        public TermToken(String term, boolean isBigram) {
            this.term = term;
            this.isBigram = isBigram;
            this.isNoise = false;
        }

        public TermToken(String term, boolean isBigram, boolean isNoise) {
            this.term = term;
            this.isBigram = isBigram;
            this.isNoise = isNoise;
        }

        @Override
        public String toString() {
            return term + (isBigram ? " [bigram]" : " [unigram]");
        }
    }
}
