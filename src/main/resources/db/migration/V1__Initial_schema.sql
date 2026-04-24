-- V1: Initial Schema
-- Ursprünglich via Hibernate ddl-auto generiert, hier für Flyway Migration festgeschrieben.

CREATE TABLE IF NOT EXISTS feeds (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    last_fetched TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    extract_content BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description VARCHAR(4000),
    link VARCHAR(1000) NOT NULL UNIQUE,
    image_url VARCHAR(1000),
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    feed_id BIGINT NOT NULL,
    content_html TEXT,
    content_extraction_failed BOOLEAN DEFAULT false,
    CONSTRAINT fk_articles_feed FOREIGN KEY (feed_id) REFERENCES feeds(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_articles_feed_id ON articles(feed_id);

CREATE TABLE IF NOT EXISTS article_read_status (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    article_id VARCHAR(255) NOT NULL,
    read BOOLEAN,
    read_at TIMESTAMP
);
