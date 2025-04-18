CREATE TABLE category
(
    category_id UUID PRIMARY KEY,
    version     BIGINT NOT NULL DEFAULT 0,
    name        VARCHAR(500),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);