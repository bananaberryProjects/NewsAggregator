-- KI-Summary-Cache: Speichert generierte KI-Zusammenfassung als JSON
CREATE TABLE ai_summary_cache (
    id BIGSERIAL PRIMARY KEY,
    for_date DATE NOT NULL UNIQUE,
    summary_json TEXT NOT NULL,
    is_fallback BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ai_summary_cache_for_date ON ai_summary_cache(for_date);
CREATE INDEX idx_ai_summary_cache_expires_at ON ai_summary_cache(expires_at);
