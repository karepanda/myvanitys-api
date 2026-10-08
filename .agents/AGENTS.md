# AGENTS.md — MyVanitys API

Spring Boot REST API for the MyVanitys product catalog, reviews, Google OAuth, and JWT authentication.

## Stack and structure
- Java 25, Spring Boot 4.1.1, Maven, PostgreSQL, Flyway, JPA/Hibernate, MapStruct, and Lombok.
- Follow the hexagonal architecture in `auth` and `product`, with shared code under `common`.
- Keep domain independent of Spring, application independent of persistence, and primary adapters independent of secondary adapters.

## Commands
```bash
mvn clean compile
mvn test
mvn clean test jacoco:report jacoco:check
mvn failsafe:integration-test failsafe:verify
mvn clean verify
mvn test-compile org.pitest:pitest-maven:mutationCoverage
mvn package -DskipTests
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Conventions and domain rules
- Unit tests end in `*Test.java`; integration tests end in `*IT.java`. Do not mix their Maven phases.
- Use plain JUnit 5 and Mockito for domain/application tests; use the established PostgreSQL and Spring fixtures for integration tests.
- Keep MapStruct mappers in the existing context-specific mapper packages and use `componentModel = "spring"`.
- Follow each bounded context's existing naming; do not unify `auth` and `product` conventions incidentally.
- Never edit generated `com.myvanitys.api.rest.v1.*` classes; change the API specification repository instead.
- Add schema changes as reviewed Flyway migrations and keep Hibernate schema validation enabled.

## Working agreement
- Read `MEMORY.md` before starting work. Update it only for durable state, decisions, lessons, or next steps.
- Keep changes focused; preserve architectural boundaries and fix violations rather than weakening `ArchitectureTest`.
- Ask before adding dependencies or changing API contracts, persistence schemas, migrations, authentication, or coverage gates.
- Never commit credentials, tokens, personal data, generated code, local configuration, or build output.
- Use `../README.md` for setup, `DEVELOPMENT.md` for IDE notes, and `doc/ARCHITECTURE.md` for design detail.

## Verification
- Run the smallest relevant unit or integration tests first; use `mvn clean verify` when the change crosses layers or persistence.
- Summarize changed behavior, checks run, and any durable decision; never store secrets or routine activity in memory.
