-- Artikel-Inhalt-Extraktion: HTML-Content Spalte hinzufügen
ALTER TABLE articles ADD COLUMN IF NOT EXISTS content_html TEXT;

-- Index für schnellere Suche (falls später Content durchsucht werden soll)
CREATE INDEX IF NOT EXISTS idx_articles_content_exists ON articles(content_html) WHERE content_html IS NOT NULL;

-- Kommentar zur Spalte
COMMENT ON COLUMN articles.content_html IS 'Extrahierter HTML-Inhalt des Artikels mittels Readability4J';
