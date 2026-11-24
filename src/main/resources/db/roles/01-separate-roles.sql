-- SEC-08: separate migration (DDL) and runtime (DML) database roles.
--
-- Run once per database as a superuser (or a sufficiently privileged role with CREATEROLE), e.g.:
--
--   # Load the migration and application passwords from the deployment secret store first.
--   # READONLY_DB_PASSWORD is optional; leave it unset unless provisioning/rotating that role.
--   psql "$DATABASE_URL" \
--     -v ON_ERROR_STOP=1 \
--     -v database_name="$PGDATABASE" \
--     -f src/main/resources/db/roles/01-separate-roles.sql
--
-- Idempotent; safe to re-run. It provisions:
--   * myvanitys_migration - runs Flyway and owns the schema objects (DDL).
--   * myvanitys_app       - the application runtime (SELECT/INSERT/UPDATE/DELETE only).
--   * myvanitys_readonly  - optional human database inspection access (SELECT only).
--
-- For an existing database previously created under a single role, transfer ownership of the
-- existing objects to the migration role using the targeted statements in
-- doc/operations/SEC-08-separate-db-roles.md (do NOT use REASSIGN OWNED, which also reassigns
-- databases and other shared objects).

-- Read secrets from the environment rather than psql command-line arguments, where they would
-- be visible to process inspection. The deployment paths provide these environment variables.
\getenv migration_password FLYWAY_PASSWORD
\getenv app_password APP_DB_PASSWORD
\getenv readonly_password READONLY_DB_PASSWORD

SELECT CASE
         WHEN EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'myvanitys_migration')
           THEN 'ALTER ROLE myvanitys_migration WITH LOGIN PASSWORD ' || quote_literal(:'migration_password')
         ELSE 'CREATE ROLE myvanitys_migration LOGIN PASSWORD ' || quote_literal(:'migration_password')
       END
\gexec

SELECT CASE
         WHEN EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'myvanitys_app')
           THEN 'ALTER ROLE myvanitys_app WITH LOGIN PASSWORD ' || quote_literal(:'app_password')
         ELSE 'CREATE ROLE myvanitys_app LOGIN PASSWORD ' || quote_literal(:'app_password')
       END
\gexec

\if :{?readonly_password}
SELECT CASE
         WHEN EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'myvanitys_readonly')
           THEN 'ALTER ROLE myvanitys_readonly WITH LOGIN PASSWORD ' || quote_literal(:'readonly_password')
         ELSE 'CREATE ROLE myvanitys_readonly LOGIN PASSWORD ' || quote_literal(:'readonly_password')
       END
\gexec
\endif

-- PostgreSQL 15+ withholds CREATE on the public schema from PUBLIC, but databases upgraded from
-- PostgreSQL 14 or earlier still grant it. Revoke it so only the migration role can create
-- objects, and defensively revoke it from the runtime role as well.
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE CREATE ON SCHEMA public FROM myvanitys_app;

-- Both roles connect to the application database.
GRANT CONNECT ON DATABASE :"database_name" TO myvanitys_migration, myvanitys_app;

-- The migration role creates and manages objects in the public schema (DDL).
GRANT USAGE, CREATE ON SCHEMA public TO myvanitys_migration;

-- The runtime role references existing objects but cannot create or alter them.
GRANT USAGE ON SCHEMA public TO myvanitys_app;

-- Objects created by the migration role automatically grant DML to the runtime role.
ALTER DEFAULT PRIVILEGES FOR ROLE myvanitys_migration IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO myvanitys_app;
ALTER DEFAULT PRIVILEGES FOR ROLE myvanitys_migration IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO myvanitys_app;

-- Objects that already exist (for example, created under the previous single-role setup).
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO myvanitys_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO myvanitys_app;

-- The optional human inspection role can read existing and future tables, but cannot write data,
-- use sequences, or create objects.
\if :{?readonly_password}
GRANT CONNECT ON DATABASE :"database_name" TO myvanitys_readonly;
GRANT USAGE ON SCHEMA public TO myvanitys_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO myvanitys_readonly;
ALTER DEFAULT PRIVILEGES FOR ROLE myvanitys_migration IN SCHEMA public
    GRANT SELECT ON TABLES TO myvanitys_readonly;
\endif
