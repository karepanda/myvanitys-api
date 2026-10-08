# Verification Matrix

Classify the changed behavior and run the smallest meaningful check first. Use the Maven wrapper for the current platform.

## Change-to-check mapping

| Change type | Focused checks | Broader checks |
|---|---|---|
| Domain model/value object | Target `*Test` | All unit tests; JaCoCo; PIT when behavior is critical |
| Application use case/command/query | Target use-case and command tests | All unit tests; JaCoCo; PIT when critical |
| Primary adapter/controller/mapper | Target controller/mapper unit test | Controller `*IT`; all unit tests; `clean verify` when contract changed |
| Secondary adapter without real I/O | Target adapter unit test | Related adapter `*IT` when persistence/provider semantics matter |
| JPA entity/repository/query | Mapper/adapter unit tests | Focused PostgreSQL `*IT`; Hibernate validation; `clean verify` |
| Flyway migration | Migration/persistence integration test | All relevant `*IT`; `clean verify` |
| Authentication/JWT/filter | Target security tests | Auth controller/client `*IT`; all unit tests; `clean verify` |
| Package/port/adapter boundary | `ArchitectureTest` | All unit tests; `clean verify` when behavior also changed |
| API-spec consumption | Spec generation/compile; backend compile; target controller test | Controller `*IT`; `ArchitectureTest`; backend `clean verify` |
| Configuration/dependency | Compile/startup-focused test | `clean verify`; profile-specific smoke check when authorized |
| Documentation only | Link/path/content inspection | No Maven unless executable claims require proof |

## Commands

Focused unit test:

```powershell
.\mvnw.cmd -Dtest=CreateProductTest test
```

Architecture test:

```powershell
.\mvnw.cmd -Dtest=ArchitectureTest test
```

All unit tests:

```powershell
.\mvnw.cmd test
```

Unit coverage gate:

```powershell
.\mvnw.cmd clean test jacoco:report jacoco:check
```

Focused integration test:

```powershell
.\mvnw.cmd -Dit.test=ProductRepositoryAdapterIT failsafe:integration-test failsafe:verify
```

All configured verification:

```powershell
.\mvnw.cmd clean verify
```

Mutation testing for deliberate domain/application confidence:

```powershell
.\mvnw.cmd test-compile org.pitest:pitest-maven:mutationCoverage
```

Use equivalent `./mvnw` commands on POSIX.

## Failure classification

- **Product failure:** The implementation violates expected behavior. Fix in scope and rerun the failing check.
- **Test failure:** The test encodes stale or incorrect expectations. Correct it only after confirming the intended behavior.
- **Environment failure:** Docker, PostgreSQL, credentials, package access, or network is unavailable. Run remaining safe checks and report the exact unverified surface.
- **Unrelated pre-existing failure:** Prove it is outside the changed behavior and report it without modifying unrelated code.
- **Flaky/non-deterministic failure:** Reproduce with a focused command and remove time/order/shared-state dependence; do not normalize repeated retries as success.

## Evidence standard

For every command, record the command, exit result, and relevant test count/failure. Never write "all tests pass" when only a focused test ran. When a check is skipped, name the missing prerequisite and the risk it leaves.
