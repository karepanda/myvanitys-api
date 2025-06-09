CREATE TABLE product_user
(
    product_user_id UUID PRIMARY KEY,
    version         BIGINT NOT NULL DEFAULT 0,
    user_id         UUID   NOT NULL,
    product_id      UUID   NOT NULL,
    deleted_at      TIMESTAMP,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT uq_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX idx_product_user_user_id ON product_user (user_id);
CREATE INDEX idx_product_user_product_id ON product_user (product_id);