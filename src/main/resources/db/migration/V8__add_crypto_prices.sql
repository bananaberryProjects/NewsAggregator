CREATE TABLE crypto_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coin_id VARCHAR(20) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    price_usd DECIMAL(18, 8) NOT NULL,
    price_eur DECIMAL(18, 8),
    market_cap_usd BIGINT,
    volume_24h_usd BIGINT,
    price_change_24h DECIMAL(18, 8),
    price_change_percentage_24h DECIMAL(10, 4),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_crypto_price_coin UNIQUE (coin_id)
);

CREATE TABLE crypto_price_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coin_id VARCHAR(20) NOT NULL,
    price_usd DECIMAL(18, 8) NOT NULL,
    price_eur DECIMAL(18, 8),
    recorded_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crypto_prices_coin ON crypto_prices(coin_id);
CREATE INDEX idx_crypto_price_history_coin_time ON crypto_price_history(coin_id, recorded_at);
