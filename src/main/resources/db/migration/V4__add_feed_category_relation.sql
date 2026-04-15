-- Verknüpfungstabelle für Feed-Kategorien (Many-to-Many)
CREATE TABLE IF NOT EXISTS feed_categories (
    feed_id UUID NOT NULL REFERENCES feeds(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (feed_id, category_id)
);

-- Indexe für schnellere Abfragen
CREATE INDEX IF NOT EXISTS idx_feed_categories_feed ON feed_categories(feed_id);
CREATE INDEX IF NOT EXISTS idx_feed_categories_category ON feed_categories(category_id);
