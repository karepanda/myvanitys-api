CREATE TABLE product (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         category_id UUID REFERENCES category(id) ON DELETE SET NULL,
                         brand VARCHAR(500),
                         name VARCHAR(255) NOT NULL,
                         color_hex INT,
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP
);
