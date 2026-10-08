# Migration Checklist

Use this checklist for every Flyway/JPA schema change.

## Discover the current state

- Find the highest existing migration version; never assume the next number from memory.
- Read every migration that created or previously altered the affected table.
- Inspect the JPA entity, MapStruct mapper, domain model, repository port, adapter, and PostgreSQL integration tests.
- Identify seeded rows and fixed identifiers that a new constraint or backfill must preserve.
- Check production profiles for Flyway locations, baseline policy, and Hibernate validation.

## Design before editing

Record:

- Existing and target columns/types.
- Expected existing-row count and possible invalid/null values.
- Backfill source and deterministic transformation.
- Nullability and default semantics after migration.
- Unique constraints and the exact duplicate policy.
- Foreign-key target and delete/update behavior.
- Query-driven indexes and their column order.
- Compatibility with the currently deployed application during rollout.

Do not infer business semantics such as normalization, ownership, or deletion policy solely from a database name.

## Safe migration patterns

For a new required field on a populated table:

1. Add it nullable or with a safe transitional default.
2. Backfill existing rows deterministically.
3. Detect invalid or duplicate rows before adding a constraint.
4. Add the final `NOT NULL`, unique, foreign-key, or check constraint.
5. Remove a transitional default when new writes must supply the value.

For moving data between product and product-user levels, define whether the original value remains a reference fallback, is copied to every association, or is intentionally left null. Do not destroy the source column until the contract, read path, and rollout establish that it is safe.

## Persistence alignment

- Match PostgreSQL types, lengths, nullability, uniqueness, and names in the JPA entity.
- Keep database-generated timestamps/versions consistent with existing entity callbacks and tests.
- Update MapStruct mapping in both directions and cover null/partial values deliberately.
- Add repository-port methods for business capabilities, not JPA-specific queries.
- Keep transactional orchestration in application/adapter boundaries already used by the context.

## Tests

Cover as applicable:

- Clean schema creation through all migrations.
- Upgrade from the prior migration state with representative existing data.
- Backfilled values and preservation of unrelated rows.
- `NOT NULL`, unique, check, and foreign-key behavior.
- Cascade/restrict behavior.
- Entity-to-domain and domain-to-entity mappings.
- Repository reads/writes using real PostgreSQL.
- Hibernate startup validation.

Use focused Failsafe execution when practical:

```powershell
.\mvnw.cmd -Dit.test=ProductUserRepositoryAdapterIT failsafe:integration-test failsafe:verify
```

Use `clean verify` when the migration affects multiple persistence paths or application layers.

## Handoff

State whether the migration is backward compatible, whether it requires coordinated deployment, what happens to existing rows, and whether any follow-up contract/application cleanup remains. Never describe a migration as reversible unless a tested recovery path exists.
