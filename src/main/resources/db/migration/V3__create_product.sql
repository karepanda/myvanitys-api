CREATE TABLE product
(
    product_id  UUID PRIMARY KEY,
    category_id UUID         REFERENCES category (category_id) ON DELETE SET NULL,
    brand       VARCHAR(500),
    name        VARCHAR(255) NOT NULL,
    color_hex   VARCHAR(7),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);
