---
name: myvanitys-database-evolution
description: Implement and verify PostgreSQL schema changes in myvanitys-api using Flyway, JPA entities, domain mappings, repository adapters, and integration tests. Use when a backend change adds or changes tables, columns, constraints, indexes, relationships, or data backfills.
---

# MyVanitys Database Evolution

Evolve the PostgreSQL schema explicitly while keeping domain, persistence, and migration responsibilities separate.

## Required workflow

1. Read `.agents/AGENTS.md`, `.agents/MEMORY.md`, existing Flyway migrations, affected JPA entities, MapStruct mappers, repository ports/adapters, and integration fixtures.
2. Read [references/migration-checklist.md](references/migration-checklist.md).
3. Confirm that the current request authorizes the schema change. If it does not, stop before creating a migration.
4. Describe the current schema, target schema, data transformation, compatibility window, and failure risks before editing.
5. Add a new, correctly ordered Flyway migration. Never modify an applied migration.
6. Update persistence entities and mappers without leaking JPA types or annotations into the domain.
7. Change domain models and repository ports only when business behavior requires it.
8. Test schema creation, repository behavior, constraints, mappings, and upgrade/backfill behavior in PostgreSQL.
9. Run Hibernate validation and the relevant integration/verification suite.

## Non-negotiable rules

- Flyway is the schema source of truth; Hibernate remains in validation mode.
- Define nullability, defaults, uniqueness, foreign keys, delete behavior, indexes, and backfills explicitly.
- Use an expand/backfill/constrain sequence when existing rows make a direct constraint unsafe.
- Keep JPA entities under infrastructure persistence and MapStruct mappers in their context-specific mapper packages.
- Keep application code dependent on repository ports, never JPA repositories or persistence entities.
- Preserve PostgreSQL semantics in integration tests. A database-backed test is an `*IT`, never a unit `*Test`.
- Do not add destructive cleanup, production data changes, or deployment steps beyond the authorized migration.

## Completion evidence

Report the migration added, data/backfill assumptions, entity/mapping/port changes, constraints and indexes, PostgreSQL tests run, Hibernate validation result, and any rollout compatibility requirement.
