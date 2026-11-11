-- Restrict stored category codes to the eight values exposed by the API contract.
-- All seeded categories already carry one of these codes (V10), so this constraint
-- applies to existing rows without a backfill.
ALTER TABLE category
  ADD CONSTRAINT chk_category_code
  CHECK (code IN ('FACE', 'EYES', 'LASHES', 'BROWS', 'LIPS', 'CREAM', 'SERUM', 'TONER'));
