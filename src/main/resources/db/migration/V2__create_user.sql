CREATE TABLE "user"
(
    user_id    UUID PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    version    BIGINT       NOT NULL DEFAULT 0,
    email      VARCHAR(200),
    name       VARCHAR(500),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);
