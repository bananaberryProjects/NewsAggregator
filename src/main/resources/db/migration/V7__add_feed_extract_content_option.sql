-- Feed-spezifische Content-Extraktion Option
ALTER TABLE feeds ADD COLUMN IF NOT EXISTS extract_content BOOLEAN DEFAULT TRUE;

-- Kommentar
COMMENT ON COLUMN feeds.extract_content IS 'FALSE wenn Content nicht extrahiert werden soll (z.B. bei Paywalls)';
