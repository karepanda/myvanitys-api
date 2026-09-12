# AGENTS.md

Agent and contributor guide for `myvanitys-api`. Documentation only — read this before changing code.

## What this is

Spring Boot REST API for MyVanitys (product catalog, reviews, Google OAuth + JWT auth). Hexagonal architecture with two bounded contexts (`auth`, `product`) and a shared `common` package.

## Tech stack (from pom.xml)

| Item | Version |
|---|---|
| Java | 25 (`<java.version>`, `.tool-versions` pins `temurin-25.0.4+101.0.LTS`) |
| Spring Boot | 4.1.1 (parent) |
| Spring Framework | 7.x (via Boot 4.1.1) |
| Jackson | 3 (`tools.jackson`) |
| Hibernate / JPA | 7 / Jakarta Persistence 3.2 |
| PostgreSQL | 16 (prod `postgres-ssl:16`; tests run PG16) |
| Flyway | core 12.4.0 (managed) |
| MapStruct | 1.6.3 |
| Lombok | 1.18.48 |
| jjwt | 0.13.0 (jjwt-jackson uses Jackson 2, present transitively) |
| Testcontainers | 2.0.5 |
| ArchUnit | 1.5.0 |
| Mockito | 5.23.0 |
| JaCoCo | 0.8.15 (unit line coverage gate **0.68**) |
| PIT (pitest-maven) | 1.30.0 (mutation threshold **65**) |
| API spec | `com.myvanitys:myvanitys-api-spec:1.11.0-SNAPSHOT` (GitHub Packages, profile `github` active by default) |

## Build and test commands

Real Maven goals only. `mvn clean verify` and any test touching the DB need Docker (Testcontainers 2.x / Zonky embedded PostgreSQL 16).

```bash
mvn clean compile                                             # compile main
mvn test                                                      # unit tests (surefire: **/*Test.java)
mvn clean verify                                              # unit + integration (failsafe: **/*IT.java) + JaCoCo check — Docker required
mvn clean test jacoco:report jacoco:check                     # unit tests + coverage gate (CI unit job)
mvn failsafe:integration-test failsafe:verify                 # integration tests only (CI integration job)
mvn test-compile org.pitest:pitest-maven:mutationCoverage     # PIT mutation testing (threshold 65)
mvn package -DskipTests                                       # build jar
mvn spring-boot:run -Dspring-boot.run.profiles=local          # run locally
```

Coverage note: the `0.68` JaCoCo gate applies to `jacoco.exec` (unit tests) and excludes `**/infrastructure/config/**` and `MyVanitysApiApplication`. Integration coverage is a separate report (`jacoco-it.exec`, `target/site/jacoco-it`).

## Architecture boundaries (enforced by ArchitectureTest)

`src/test/java/com/myvanitys/api/ArchitectureTest.java` defines 7 active ArchUnit rules — all green. Breaking one is a regression to fix, not a rule to loosen:

1. `domain_does_not_depend_on_infrastructure`
2. `domain_does_not_depend_on_application`
3. `primary_adapters_do_not_depend_on_secondary_adapters`
4. `controllers_only_in_primary_adapter`
5. `repository_adapters_only_in_secondary_adapter`
6. `domain_does_not_depend_on_spring`
7. `application_does_not_depend_on_persistence`

OpenAPI-generated classes under `com.myvanitys.api.rest.v1.*` (from the spec dependency, delegate pattern `*ApiDelegate`/`*ApiController`) are excluded from these rules. Do not edit generated code; change the spec.

## Naming conventions

- Unit tests: `*Test.java` (surefire). Integration tests: `*IT.java` (failsafe). Never mix.
- Lombok: `@Getter`/`@RequiredArgsConstructor`/`@AllArgsConstructor`/`@Builder` on models and adapters.
- MapStruct: mappers are `@Mapper(componentModel = "spring")`; mappers live in `infrastructure/persistence/mapper` (product) or `infrastructure/adapter/primary/mapper` (web responses).

### Known inconsistency: auth vs product naming

Equivalent concepts are named differently across the two contexts. This is tolerated (no ArchUnit rule targets it) — follow each context's existing style, do not "unify" without a dedicated refactor.

| Concept | auth | product |
|---|---|---|
| Use case implementations | `application.service` (`RegisterUser`, `GoogleAuthentication`) | `application.usecase` (`CreateProduct`, `FindProductByUser`, ...) |
| Commands | `application.port.primary.command` | `application.command` |
| Application diagrams | `ClassDiagramUmlAuth*.mermaid` (PascalCase + `Uml`) | `classDiagramProduct*.mermaid` (camelCase) |

## Testing conventions

- Domain/application logic: plain JUnit 5 + Mockito. No Spring context.
- Persistence: `@DataJpaTest` + Zonky embedded Postgres (`@AutoConfigureEmbeddedDatabase`) or Testcontainers 2.x — see `AbstractJpaProductTest`, `AbstractJpaAuthTest`, `AbstractIntegrationTest`.
- Web/auth flows: `@SpringBootTest` + `@AutoConfigureMockMvc` (`org.springframework.boot.webmvc.test.autoconfigure`) — see `*ControllerTestIT`.
- Do not mock `Jwts`/`ObjectMapper` in JWT tests: `JwtTokenGeneratorAdapterTest` exercises the real jjwt/Jackson serialization on purpose.

## Java tooling

Java is managed with **asdf**. `.tool-versions` pins `java temurin-25.0.4+101.0.LTS`. Run `asdf install` before building.

## More

- Architecture detail and diagrams: `doc/ARCHITECTURE.md`
- Entity relationships: `doc/MER.mermaid`
- Local setup: `README.md`
