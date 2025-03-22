CREATE TABLE category
(
    category_id UUID PRIMARY KEY,
    name        VARCHAR(500),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);