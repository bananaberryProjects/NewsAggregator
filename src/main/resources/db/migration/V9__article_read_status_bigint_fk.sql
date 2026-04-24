-- Migration: Ändert article_id in article_read_status von VARCHAR auf BIGINT
-- und fügt einen Foreign Key auf articles(id) hinzu.
-- Daten bleiben erhalten (numerische Strings werden konvertiert).

-- 1. Temporäre BIGINT-Spalte hinzufügen
ALTER TABLE article_read_status ADD COLUMN article_id_new BIGINT;

-- 2. Daten migrieren: String -> BigInt (nur wo numerisch)
UPDATE article_read_status
SET article_id_new = article_id::BIGINT
WHERE article_id ~ '^[0-9]+$';

-- 3. Alte Spalte entfernen
ALTER TABLE article_read_status DROP COLUMN article_id;

-- 4. Neue Spalte umbenennen
ALTER TABLE article_read_status RENAME COLUMN article_id_new TO article_id;

-- 5. NOT NULL setzen (wie bisher)
ALTER TABLE article_read_status ALTER COLUMN article_id SET NOT NULL;

-- 6. Foreign Key auf articles(id) hinzufügen
ALTER TABLE article_read_status
ADD CONSTRAINT fk_article_read_status_article
FOREIGN KEY (article_id) REFERENCES articles(id)
ON DELETE CASCADE;

-- 7. Index für schnellere Lookups
CREATE INDEX idx_article_read_status_article_id ON article_read_status(article_id);
