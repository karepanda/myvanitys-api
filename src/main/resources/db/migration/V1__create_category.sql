CREATE TABLE category
(
    category_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100)
);
