# SEC-34 PostgreSQL restore drill

## Outcome

The restore drill completed successfully on 2026-11-23. A fresh logical backup of the production PostgreSQL 16 database was encrypted before it was persisted, restored into a separate PostgreSQL service and volume, checked with representative aggregate queries, and used to start the production API image. The source database was not modified.

The isolated services and volumes were removed after the checks. No credentials, backup key, plaintext dump, or row-level production data were retained in the repository or drill logs.

## Evidence

| Check | Result |
| --- | --- |
| Backup created | `2026-11-23T16:52:42Z` |
| Backup access | Railway private networking and service-variable references; no public database endpoint and no credentials printed |
| Dump format | PostgreSQL custom format, `--no-owner --no-acl` |
| Encryption | AES-256-CBC with PBKDF2-HMAC-SHA-256, 600,000 iterations, a fresh 256-bit key, and a random salt |
| Plaintext handling | `pg_dump` streamed through a named pipe into encryption; no plaintext dump was persisted |
| Encrypted size | 18,976 bytes |
| Encrypted SHA-256 | `d582d699e39132f5cdb699715973ff46d7752ed23e6467b6472d256ae35a5cc3` |
| Backup creation time | 1 second |
| `pg_restore` plus SQL verification | Less than 1 second |
| API process startup | 13.163 seconds |
| End-to-end recovery window | 3 minutes 22 seconds, from isolated database provisioning to a health-checked API deployment |
| PostgreSQL version | 16.16 |
| Flyway version restored | 11; the API reported the schema up to date |
| Representative restored data | 8 categories, 63 products, 15 users, 22 product-user relations, and 14 reviews |
| API startup | Successful Hikari connection, Flyway validation, and Railway readiness health check |

Only aggregate counts were recorded. User records, tokens, email addresses, review text, and other row-level data were not written to logs.

## Recovery procedure verified

1. Provision a PostgreSQL service with the same major version as the source, a new empty volume, no public domain or TCP proxy, and a database name dedicated to the drill.
2. Give a short-lived runner access to the source and target through Railway service-variable references. Do not copy or print rendered credentials.
3. Generate a fresh backup key in the runner's ephemeral filesystem. Keep the key separate from the backup volume.
4. Run `pg_dump` in custom format with ownership and ACL metadata excluded. Stream the dump through a named pipe into `openssl enc -aes-256-cbc -salt -pbkdf2 -iter 600000`; persist only the ciphertext.
5. Record the ciphertext size and SHA-256 digest. Confirm both before beginning recovery.
6. Decrypt through a second named pipe and run `pg_restore --exit-on-error --no-owner --no-acl` against the isolated database. Check the exit status of both decryption and restore processes.
7. Query the restored Flyway history and representative table counts. Do not log row-level data.
8. Start the production API image with its PostgreSQL variables referencing the isolated service. Require the readiness health check to pass and confirm that Flyway reports no pending migration.
9. Destroy the ephemeral backup key, runner, encrypted backup volume, API instance, restored database, and restored database volume.

## Access and isolation notes

- The production database had no public endpoint. The runner reached it only over Railway's private network.
- The source connection values remained Railway references and were never rendered in repository files or logs.
- The restore target used a distinct service and volume and was never wired to the production API.
- Temporary non-secret OAuth and JWT values were used solely to allow the isolated API process to initialize.
- The backup key existed only in the runner's ephemeral filesystem and was deleted immediately after the successful restore.
- A repeat restore against the already populated target failed fast on the first existing relation. Recovery must always use a new empty target; this procedure is intentionally not an in-place restore.

## Remaining gap

Railway point-in-time recovery was disabled and the backup inventory was empty when the drill began. Railway also rejected an on-demand managed backup because that capability is unavailable on the current workspace plan. This drill therefore validated a freshly created encrypted logical backup, not a retained scheduled backup.

A separate follow-up should establish a recurring backup using managed encrypted storage or an approved authenticated-encryption format, with retention and alerting; store its recovery key in an approved secret manager separate from the backup; and repeat this drill using one of those retained artifacts. Until that is in place, the measured recovery procedure is valid but there is no standing recovery point to use during an unplanned incident.
