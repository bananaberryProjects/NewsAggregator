-- Kategorien-Tabelle erstellen
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(7) DEFAULT '#667eea',
    icon VARCHAR(50) DEFAULT 'label',
    created_at TIMESTAMP
);

-- Verknüpfungstabelle Feed-Categories (Many-to-Many)
CREATE TABLE IF NOT EXISTS feed_categories (
    feed_id UUID REFERENCES feeds(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (feed_id, category_id)
);

-- Index für schnelleres Suchen
CREATE INDEX IF NOT EXISTS idx_feed_categories_feed_id ON feed_categories(feed_id);
CREATE INDEX IF NOT EXISTS idx_feed_categories_category_id ON feed_categories(category_id);

-- Standard-Kategorien einfügen
INSERT INTO categories (id, name, color, icon, created_at) VALUES
    (gen_random_uuid(), 'Technik', '#3b82f6', 'computer', NOW()),
    (gen_random_uuid(), 'Wirtschaft', '#10b981', 'trending_up', NOW()),
    (gen_random_uuid(), 'Sport', '#f59e0b', 'sports', NOW()),
    (gen_random_uuid(), 'Unterhaltung', '#ec4899', 'movie', NOW()),
    (gen_random_uuid(), 'Krypto', '#8b5cf6', 'currency_bitcoin', NOW()),
    (gen_random_uuid(), 'News', '#64748b', 'newspaper', NOW())
ON CONFLICT (name) DO NOTHING;
