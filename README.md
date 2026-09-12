# myvanitys-api

REST API for MyVanitys: product catalog, reviews, and Google OAuth + JWT authentication. Spring Boot 4.1.1, Java 25, hexagonal architecture.

- Architecture, boundaries, and diagrams: [doc/ARCHITECTURE.md](doc/ARCHITECTURE.md)
- Contributor/agent guide: [AGENTS.md](AGENTS.md)

## For API consumers

Building a client against this API (mobile, web, or any backend)? Start with [doc/API_OVERVIEW.md](doc/API_OVERVIEW.md) — authentication, core concepts, endpoints, and the error contract. The rest of this README is contributor setup.

---

## Prerequisites

- Java 25 via asdf: `asdf install` (pins `temurin-25.0.4+101.0.LTS` in `.tool-versions`)
- Docker (for local PostgreSQL and for integration tests)
- Access to GitHub Packages for `com.myvanitys:myvanitys-api-spec` (configure a `github` server in `~/.m2/settings.xml`)

## Local setup

1. Start PostgreSQL:
   ```bash
   docker compose up -d
   ```
   The `postgres` service exposes `localhost:5432` with database `myvanitysdb`, user `myvanitys`, password `secret`.

2. Export Google OAuth credentials (required by the `local` profile):
   ```bash
   export GOOGLE_CLIENT_ID=your-client-id
   export GOOGLE_CLIENT_SECRET=your-client-secret
   ```

3. Run the app:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   Base URL `http://localhost:8080/myvanitys` (context path `/myvanitys`). Health: `http://localhost:8080/myvanitys/actuator/health`.

Flyway runs migrations on startup; Hibernate uses `ddl-auto: validate`.

## Build and test

```bash
mvn clean compile                       # compile
mvn test                                # unit tests (no Docker)
mvn clean verify                        # unit + integration + JaCoCo gate (Docker required)
mvn package -DskipTests                 # build jar
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # mutation testing
```

Full command reference: [AGENTS.md](AGENTS.md).
