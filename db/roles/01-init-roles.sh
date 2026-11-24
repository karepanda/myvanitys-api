#!/bin/bash
set -euo pipefail

# SEC-08: create the migration and runtime database roles on first database initialization.
# Executed by the PostgreSQL image's docker-entrypoint as POSTGRES_USER (superuser).
: "${FLYWAY_PASSWORD:?FLYWAY_PASSWORD is required}"
: "${APP_DB_PASSWORD:?APP_DB_PASSWORD is required}"

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  -v database_name="$POSTGRES_DB" \
  -f /db/roles/01-separate-roles.sql
