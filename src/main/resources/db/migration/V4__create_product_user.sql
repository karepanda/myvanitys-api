-- Crear tabla product_user
CREATE TABLE product_user (
    product_user_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    product_id UUID NOT NULL,
    delete_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product(product_id),
    CONSTRAINT uq_user_product UNIQUE (user_id, product_id)
);

-- Crear tabla review
CREATE TABLE review (
    review_id UUID PRIMARY KEY,
    product_user_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    description VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_product_user FOREIGN KEY (product_user_id) REFERENCES product_user(product_user_id)
);

-- Crear índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_product_user_user_id ON product_user(user_id);
CREATE INDEX idx_product_user_product_id ON product_user(product_id);
CREATE INDEX idx_review_product_user_id ON review(product_user_id);