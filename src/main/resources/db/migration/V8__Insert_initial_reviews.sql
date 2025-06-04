-- V8__Insert_initial_reviews.sql

-- First, we need to insert the products into the product table
-- User 1 with Face product
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('01973688-969f-74f5-a381-df0774654715',
        '0197368a-44a8-77be-9bc9-397ab3ec539c',
        '111e4567-e89b-12d3-a456-426614174000',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- User 2 with Eyes product
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-2993-79bd-9f89-54ed7bc864ff',
        '0197368b-4bea-78d2-9d78-c7a88799a622',
        '222e4567-e89b-12d3-a456-426614174001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- User 3 con producto Eyelash
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-6c8c-7d60-8bf3-4ec5cde240ac',
        '0197368b-841d-7c3f-92d4-0aa6205f5e34',
        '333e4567-e89b-12d3-a456-426614174002',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- User 1 with Eyes product (for multiple reviews)
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-990f-7764-afe8-775d4b053264',
        '0197368b-b408-7e03-8b79-4b07d6d0b70f',
        '222e4567-e89b-12d3-a456-426614174001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- User 2 with Eyelash product (for multiple reviews)
INSERT INTO product_user (product_user_id, user_id, product_id, created_at, updated_at)
VALUES ('0197368b-c9b1-7f57-81db-d108f7e9df9e',
        '0197368b-e388-7473-a5fc-15ddc8d22b00',
        '333e4567-e89b-12d3-a456-426614174002',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- REVIEWS for Maybelline Fit Me Foundation (6 reviews)
INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-5553-7e10-be26-c5972a530180',
        '01973688-969f-74f5-a381-df0774654715',
        5,
        'Okay, this blush is EVERYTHING. I got it in Joy and its just the perfect peachy-nude for my light-medium skin. You literally only need one tiny dot for each cheek, seriously! I use a damp beauty sponge to blend it out and it just melts into my skin. It gives such a natural, healthy flush, like I have just been on a brisk walk. It lasts all day on me without fading. If you are on the fence, just get it – you wont regret it!',
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        CURRENT_TIMESTAMP - INTERVAL '10 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-746f-72e5-bde9-d1fbdbdc12ee',
        '01973688-969f-74f5-a381-df0774654715',
        4,
        'Covers my dark circles without creasing. A must-have for me.',
        CURRENT_TIMESTAMP - INTERVAL '7 days',
        CURRENT_TIMESTAMP - INTERVAL '7 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-9298-7278-88a8-9769e12db248',
        '01973688-969f-74f5-a381-df0774654715',
        3,
        'Spectacular, buildable glow. Love it for a natural or intense look.',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('01973a83-0fbb-797b-8c1d-1604cbc35ab3',
        '01973688-969f-74f5-a381-df0774654715',
        2,
        'Got this because it was supposed to be hydrating and not crease. Lies! It settled into every single fine line under my eyes almost immediately, making them look even worse. It also didnt provide much coverage for my dark circles',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('01973a89-1fa1-767b-a641-60ef62509b04',
        '01973688-969f-74f5-a381-df0774654715',
        1,
        'This foundation was such a disappointment. It settled into every single line and pore on my face, making me look about 10 years older',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('01973a8f-133a-7038-9c75-8da5ca129c65',
        '01973688-969f-74f5-a381-df0774654715',
        5,
        'Covers my dark circles without creasing. A must-have for me.',
        CURRENT_TIMESTAMP - INTERVAL '5 days',
        CURRENT_TIMESTAMP - INTERVAL '5 days');

-- REVIEWS for Urban Decay Naked Eyeshadow Palette (2 reviews)
INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-af58-79ea-82a1-980ddd676fdd',
        '0197368b-2993-79bd-9f89-54ed7bc864ff',
        5,
        'Okay, this palette is a classic for a reason! Every single shade is so buttery and blends like a dream. I reach for this for everyday looks, but also for special occasions because you can easily build up the intensity.',
        CURRENT_TIMESTAMP - INTERVAL '12 days',
        CURRENT_TIMESTAMP - INTERVAL '12 days');

INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-c58a-708b-b041-d3d74a5297c1',
        '0197368b-2993-79bd-9f89-54ed7bc864ff',
        4,
        'My favorite everyday palette! I have the Warm Nude one, and the shades are just stunning.',
        CURRENT_TIMESTAMP - INTERVAL '3 days',
        CURRENT_TIMESTAMP - INTERVAL '3 days');

-- REVIEWS for Benefit They're Real! Mascara (1 review)
INSERT INTO review (review_id, product_user_id, rating, comment, created_at, updated_at)
VALUES ('0197368c-dc7e-7f2d-b9ec-4c741112e205',
        '0197368b-6c8c-7d60-8bf3-4ec5cde240ac',
        4,
        '"Heard so much hype about this mascara, and it actually lives up to it! My lashes are naturally pretty short and straight, but this stuff makes them look so long and lifted. It separates them beautifully, too, no clumping at all. I love the flexible wand, it really gets to every lash. It holds a curl well and doesnt smudge on me throughout the day, even in humid weather. Its my new holy grail drugstore mascara!',
        CURRENT_TIMESTAMP - INTERVAL '1 day',
        CURRENT_TIMESTAMP - INTERVAL '1 day');