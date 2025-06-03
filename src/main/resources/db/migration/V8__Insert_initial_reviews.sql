-- V8__Insert_initial_reviews.sql

-- Primero necesitamos algunos usuarios ficticios en product_user para poder crear reviews
-- Usuario 1 con producto Face
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('01973688-969f-74f5-a381-df0774654715',
        '0197368a-44a8-77be-9bc9-397ab3ec539c',
        '111e4567-e89b-12d3-a456-426614174000',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Usuario 2 con producto Eyes  
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-2993-79bd-9f89-54ed7bc864ff',
        '0197368b-4bea-78d2-9d78-c7a88799a622',
        '222e4567-e89b-12d3-a456-426614174001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Usuario 3 con producto Eyelash
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-6c8c-7d60-8bf3-4ec5cde240ac',
        '0197368b-841d-7c3f-92d4-0aa6205f5e34',
        '333e4567-e89b-12d3-a456-426614174002',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Usuario 1 con producto Eyes (para tener múltiples reviews)
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-990f-7764-afe8-775d4b053264',
        '0197368b-b408-7e03-8b79-4b07d6d0b70f',
        '222e4567-e89b-12d3-a456-426614174001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Usuario 2 con producto Eyelash (para tener múltiples reviews)
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-c9b1-7f57-81db-d108f7e9df9e',
        '0197368b-e388-7473-a5fc-15ddc8d22b00',
        '333e4567-e89b-12d3-a456-426614174002',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- REVIEWS para Maybelline Fit Me Foundation (3 reviews)
INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-5553-7e10-be26-c5972a530180',
        '01973688-969f-74f5-a381-df0774654715',
        5,
        'Excelente base! Se adapta perfectamente a mi tono de piel y dura todo el día sin oxidarse.',
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        CURRENT_TIMESTAMP - INTERVAL '10 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-746f-72e5-bde9-d1fbdbdc12ee',
        '0197368b-990f-7764-afe8-775d4b053264',
        4,
        'Muy buena cobertura y acabado natural. Solo le resta una estrella porque se puede ver un poco pesada en pieles secas.',
        CURRENT_TIMESTAMP - INTERVAL '7 days',
        CURRENT_TIMESTAMP - INTERVAL '7 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-9298-7278-88a8-9769e12db248',
        '0197368b-c9b1-7f57-81db-d108f7e9df9e',
        3,
        'Es una base decente por el precio, pero no es la mejor que he probado. Se nota un poco en algunas áreas.',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days');

-- REVIEWS para Urban Decay Naked Eyeshadow Palette (2 reviews)
INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-af58-79ea-82a1-980ddd676fdd',
        '0197368b-2993-79bd-9f89-54ed7bc864ff',
        5,
        'Paleta increíble! Los colores son pigmentados y se difuminan súper bien. Vale cada centavo.',
        CURRENT_TIMESTAMP - INTERVAL '12 days',
        CURRENT_TIMESTAMP - INTERVAL '12 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-c58a-708b-b041-d3d74a5297c1',
        '01973688-969f-74f5-a381-df0774654715',
        4,
        'Me encanta la variedad de tonos nude. Perfecta para looks naturales y también para smokey eyes.',
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        CURRENT_TIMESTAMP - INTERVAL '3 days');

-- REVIEWS para Benefit They're Real! Mascara (1 review)
INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-dc7e-7f2d-b9ec-4c741112e205',
        '0197368b-6c8c-7d60-8bf3-4ec5cde240ac',
        4,
        'Alarga muy bien las pestañas y no se corre durante el día. El cepillo es un poco grande pero se acostumbra.',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day');