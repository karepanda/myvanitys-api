CREATE TABLE review
(
    review_id       UUID PRIMARY KEY,
    product_user_id UUID         NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    rating          INTEGER      NOT NULL,
    comment         VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    deleted_at      TIMESTAMP,
    CONSTRAINT fk_product_user FOREIGN KEY (product_user_id) REFERENCES product_user (product_user_id)
);

CREATE INDEX idx_review_product_user_id ON review (product_user_id);