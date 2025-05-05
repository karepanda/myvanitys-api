-- Flyway script to insert initial data into the category table
-- Suggested name: V2__Insert_initial_category.sql
-- (Assuming V1 is the creation of the table)

-- Insert a test category
INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('123e4567-e89b-12d3-a456-426614174000',
        0,
        'Face',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Insert additional categories for testing
INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440001',
        0,
        'Eyes',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440002',
        0,
        'Eyelash',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('770e8400-e29b-41d4-a716-446655440003',
        0,
        'Brows',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('01969b31-b294-7939-a8d2-c298e896ec1f',
        0,
        'Lips',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('01969b32-15c7-7ff7-a712-0b42354f080e',
        0,
        'Cream',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('01969b32-73b2-7818-b543-f0a35aee0dc4',
        0,
        'Serum',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

INSERT INTO category (category_id, version, name, created_at, updated_at)
VALUES ('01969b32-e411-7df9-815f-9142e7c4f6e5',
        0,
        'Toner',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);
