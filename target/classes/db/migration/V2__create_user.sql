CREATE TABLE "user"
(
    user_id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    token  VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(200),
    name       VARCHAR(500),
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);
