-- Flyway script to insert initial data into the category table
-- Suggested name: V2__Insert_initial_category.sql
-- (Assuming V1 is the creation of the table)

-- Insert a test category
INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('123e4567-e89b-12d3-a456-426614174000',
        0,
        'Facial Care',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Insert additional categories for testing
INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001',
        0,
        'Hair Care',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440002',
        0,
        'Body Care',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('770e8400-e29b-41d4-a716-446655440003',
        0,
        'Makeup',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
