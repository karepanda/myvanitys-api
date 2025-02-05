CREATE TABLE review
(
    review_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL,
    product_id  UUID NOT NULL,
    stars       INT,
    description VARCHAR(500),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product (id)
);
