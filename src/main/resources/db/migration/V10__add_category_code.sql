-- Add a stable machine-readable code to the category table.

ALTER TABLE category ADD COLUMN code VARCHAR(50);

-- Backfill the stable codes for the seeded cosmetic categories.
UPDATE category SET code = 'FACE'   WHERE name = 'Face';
UPDATE category SET code = 'EYES'   WHERE name = 'Eyes';
UPDATE category SET code = 'LASHES' WHERE name = 'Eyelash';
UPDATE category SET code = 'BROWS'  WHERE name = 'Brows';
UPDATE category SET code = 'LIPS'   WHERE name = 'Lips';
UPDATE category SET code = 'CREAM'  WHERE name = 'Cream';
UPDATE category SET code = 'SERUM'  WHERE name = 'Serum';
UPDATE category SET code = 'TONER'  WHERE name = 'Toner';

-- The code is required and unique for every category.
ALTER TABLE category ALTER COLUMN code SET NOT NULL;
ALTER TABLE category ADD CONSTRAINT uk_category_code UNIQUE (code);
