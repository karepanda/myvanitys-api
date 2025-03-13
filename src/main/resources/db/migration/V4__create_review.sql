CREATE TABLE review
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID   NOT NULL,
    product_id  UUID   NOT NULL,
    stars       INT CHECK (rating BETWEEN 1 AND 5),
    description VARCHAR(500),
    version     BIGINT NOT NULL  DEFAULT 0,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product (id)
);
