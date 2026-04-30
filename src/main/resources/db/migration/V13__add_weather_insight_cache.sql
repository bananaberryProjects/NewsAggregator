CREATE TABLE IF NOT EXISTS weather_insight_cache (
    id BIGSERIAL PRIMARY KEY,
    location_key VARCHAR(50) NOT NULL UNIQUE,
    insight_json TEXT NOT NULL,
    is_fallback BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_weather_insight_cache_location_key ON weather_insight_cache(location_key);
CREATE INDEX IF NOT EXISTS idx_weather_insight_cache_expires_at ON weather_insight_cache(expires_at);
