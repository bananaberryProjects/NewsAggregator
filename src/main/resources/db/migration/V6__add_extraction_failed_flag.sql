-- Fehler-Markierung für Content-Extraktion
ALTER TABLE articles ADD COLUMN IF NOT EXISTS content_extraction_failed BOOLEAN DEFAULT FALSE;

-- Index für schnelle Filterung bei Bulk-Extraktion
CREATE INDEX IF NOT EXISTS idx_articles_extraction_failed ON articles(content_extraction_failed) WHERE content_extraction_failed = TRUE;

-- Kommentar
COMMENT ON COLUMN articles.content_extraction_failed IS 'TRUE wenn Content-Extraktion für diesen Artikel fehlgeschlagen ist';
