# SEC-08 Separate migration and runtime database roles

## Goal

The application runtime connects with a DML-only role (`myvanitys_app`) and Flyway connects with a separate migration role (`myvanitys_migration`) that owns the schema objects and runs DDL. The runtime role can never create, alter, or drop objects.

## Roles and privileges

| Role | Used by | Privileges |
| --- | --- | --- |
| `myvanitys_migration` | Flyway (`FLYWAY_USER` / `FLYWAY_PASSWORD`) | `CONNECT`, `USAGE` + `CREATE` on `public`; owns the tables it creates, so it can alter and drop them |
| `myvanitys_app` | Application datasource (`APP_DB_USER` / `APP_DB_PASSWORD`) | `CONNECT`, `USAGE` on `public`, and `SELECT/INSERT/UPDATE/DELETE` on tables (plus `USAGE` on sequences) |

The migration role's default privileges are configured so that every table and sequence it creates automatically grants the runtime role its DML privileges. No object is ever granted DDL to the runtime role.

## Provisioning

The provisioning script is `src/main/resources/db/roles/01-separate-roles.sql`. It is idempotent. Run it once per database as a superuser, or as an administrative role with `CREATEROLE` and sufficient privileges to administer the two application roles and grant privileges in the target database. Load `FLYWAY_PASSWORD` and `APP_DB_PASSWORD` from the deployment secret store into the process environment first; the SQL reads them with `psql`'s `\getenv`, so they are not exposed in process arguments:

```bash
psql "$DATABASE_URL" \
  -v ON_ERROR_STOP=1 \
  -v database_name="$PGDATABASE" \
  -f src/main/resources/db/roles/01-separate-roles.sql
```

The script also revokes `CREATE` on the `public` schema from `PUBLIC` and from `myvanitys_app`. PostgreSQL 15+ already withholds `CREATE` from `PUBLIC`, but databases upgraded from PostgreSQL 14 or earlier still grant it, which would let the runtime role create objects. The `REVOKE` closes that gap on upgraded databases and is defensive on new ones.

Self-hosted deployments using `docker-compose-production.yml` provision the roles automatically on first database initialization through `db/roles/01-init-roles.sh`.

### Existing database cutover

If the schema already exists under a single role, first run the provisioning script above so `myvanitys_migration` exists and has `CREATE` on `public`. Then, before the next migration runs, transfer ownership of the existing tables and sequences in `public` to that role. Use targeted ownership changes, not `REASSIGN OWNED`: PostgreSQL documents that `REASSIGN OWNED` also reassigns shared objects (databases, tablespaces) owned by the source role.

```sql
-- Run as a superuser. Replace 'previous_owner' with the role that currently owns the objects.
-- A non-superuser may run this only if it owns the objects and can SET ROLE to
-- myvanitys_migration. This touches only tables and sequences in the public schema; it leaves
-- the database, the public schema, and any tablespaces ownership unchanged.
DO $$
DECLARE r record;
BEGIN
    FOR r IN
        SELECT n.nspname AS schema, c.relname AS name, c.relkind AS kind
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'public'
          AND c.relkind IN ('r', 'S')
          AND c.relowner = (SELECT oid FROM pg_roles WHERE rolname = 'previous_owner')
    LOOP
        EXECUTE format('ALTER %s %I.%I OWNER TO myvanitys_migration',
                       CASE r.kind WHEN 'S' THEN 'SEQUENCE' ELSE 'TABLE' END,
                       r.schema, r.name);
    END LOOP;
END
$$;
```

The provisioning script must run before this block: it creates the target role and grants it `CREATE` on `public`, both of which PostgreSQL requires before ownership can be transferred. No schema or database ownership change is required.

## Required variables

| Variable | Role | Notes |
| --- | --- | --- |
| `APP_DB_USER` / `APP_DB_PASSWORD` | runtime (`myvanitys_app`) | replaces the previous single-role datasource credentials |
| `FLYWAY_USER` / `FLYWAY_PASSWORD` | migration (`myvanitys_migration`) | new; Flyway no longer inherits the datasource credentials |
| `PGHOST` / `PGPORT` / `PGDATABASE` | connection target | unchanged (Railway plugin values) |

`application-prod.yml` now wires `spring.datasource.username/password` to `APP_DB_USER`/`APP_DB_PASSWORD` and `spring.flyway.user/password` to `FLYWAY_USER`/`FLYWAY_PASSWORD`, and sets `spring.jpa.hibernate.ddl-auto: validate` so production enforces the same Hibernate schema validation the tests exercise.

## Verification

`DatabaseRoleSeparationIT` executes the real provisioning artifact (`src/main/resources/db/roles/01-separate-roles.sql`) through `psql` against PostgreSQL 16, with the role passwords supplied through the container environment as they are in deployment, and then boots the application to assert:

1. Flyway migrates the schema under `myvanitys_migration` and Hibernate validation (`ddl-auto: validate`) succeeds under `myvanitys_app`.
2. The runtime role reads seeded data and inserts/deletes application rows.
3. The runtime role is rejected (`permission denied`) when it attempts `CREATE TABLE`.

## Production cutover checklist

1. Run `src/main/resources/db/roles/01-separate-roles.sql` against production to create and configure both roles (see above). On an existing database, then transfer ownership of the existing tables/sequences to `myvanitys_migration` using the targeted `DO` block above (do not use `REASSIGN OWNED`).
2. Set `APP_DB_USER`, `APP_DB_PASSWORD`, `FLYWAY_USER`, and `FLYWAY_PASSWORD` in the deployment environment. Never print or commit these values.
3. Deploy. Confirm the app starts: Flyway reports the schema up to date, Hikari connects as `myvanitys_app`, Hibernate validation passes, and the readiness health check passes.
4. Confirm the runtime cannot run DDL, for example with a short-lived connection as `myvanitys_app`: `CREATE TABLE should_fail (id uuid);` must fail with `permission denied for schema public`.
5. Complete the restore drill (SEC-34) against the production database with the new roles in place before cutover. The drill procedure is documented in `doc/operations/SEC-34-postgresql-restore-drill.md`; re-run it using a retained backup so the measured recovery path reflects the separated roles.

## Status

The code and verification are complete, but SEC-08 is **blocked from production cutover** until step 5 of the checklist (the separated-role restore drill against a retained backup) is performed. That step requires production/Railway access and a retained backup and cannot be completed from the repository.
