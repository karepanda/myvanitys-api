# MEMORY.md — MyVanitys API

Durable project memory shared by coding agents. Keep this file concise and remove stale information.

## Current state
- Spring Boot 4.1.1 API running on Java 25 with PostgreSQL and Flyway-managed schema changes.
- The `auth` context implements Google OAuth and JWT authentication; `product` implements catalog, vanity, and review behavior.
- Hexagonal boundaries are enforced by `ArchitectureTest`.
- Unit, integration, JaCoCo, and PIT mutation checks are configured through Maven.

## Decisions and rationale
- Keep domain and application behavior isolated from framework and persistence concerns.
- Treat `myvanitys-api-spec` as the source of truth for generated REST interfaces and models.
- Use Flyway migrations with Hibernate validation so schema evolution is explicit and repeatable.
- Preserve the established naming differences between `auth` and `product` until a dedicated refactor is approved.

## Lessons and pitfalls
- Database and integration checks require PostgreSQL fixtures or Docker; plain unit tests must not start Spring.
- JWT tests intentionally exercise real jjwt/Jackson serialization instead of mocking it.
- The API specification dependency may require authenticated GitHub Packages access during builds.

## Next steps
- No durable next step is recorded.

Update this file only when durable project state, decisions, lessons, or next steps change. Move permanent rules to `AGENTS.md`. Never add secrets, tokens, personal data, commit history, or routine task logs.
