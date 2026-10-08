---
name: myvanitys-unit-testing
description: Create, update, or review fast isolated JUnit 5 unit tests for myvanitys-api domain, application, mapper, security, controller, and secondary-adapter classes. Use when backend behavior needs focused Test.java coverage or an existing unit test needs improvement. Do not use for Spring context, PostgreSQL, Flyway, WireMock, Testcontainers, or other integration tests.
---

# MyVanitys Unit Testing

Write behavior-focused unit tests that remain fast, deterministic, isolated, and consistent with the repository's JUnit 5 and Mockito practices.

## Workflow

1. Read the production class, its direct collaborators, the nearest test class, and the applicable package conventions.
2. Identify observable success, validation, error, and forbidden-side-effect behaviors. Do not derive tests from line coverage alone.
3. Decide whether the scenario is truly a unit test. If it needs Spring, PostgreSQL, Flyway, WireMock, Testcontainers, filesystem I/O, or network I/O, use an `*IT` workflow instead.
4. Read [references/unit-test-patterns.md](references/unit-test-patterns.md) for the relevant layer.
5. Add the smallest set of tests that distinguishes the required behaviors without duplicating framework or implementation details.
6. Run the focused test class. Run all unit tests when shared behavior, mappers, security, or multiple classes changed.

## Conventions

- Use JUnit Jupiter only. Do not add JUnit 4 runners or annotations.
- Mirror production packages under `src/test/java` and name unit test classes `*Test.java`; reserve `*IT.java` for integration tests.
- Default new method names to `methodName_whenCondition_thenExpectedOutcome`. Preserve an existing class's clear local convention rather than mass-renaming unrelated tests.
- Structure scenarios as Given, When, Then. Use comments only when they improve scanning.
- Test one logical behavior per method. Multiple related assertions are valid; use `assertAll` only for independent properties of the same result.
- Prefer AssertJ in new test classes. Preserve the established assertion style of an existing class unless a focused change materially improves it.
- Use fixed UUIDs and `Instant` values when identity or time is asserted. Never use `Thread.sleep`.
- Use parameterized tests for genuine input/output matrices, not to conceal unrelated scenarios.
- Use `@Nested` when it clarifies multiple public methods or coherent behavior groups.
- Keep `@BeforeEach` small and limited to shared fixtures. Keep scenario-specific data in the scenario.

## Mockito and reactive behavior

- Use `@ExtendWith(MockitoExtension.class)`, `@Mock`, and `@InjectMocks` for ordinary collaborator-based units.
- Construct the target manually when a real stateful collaborator, such as `AuthenticatedUserContext`, is part of the behavior or explicit wiring improves clarity.
- Mock ports and external collaborators, not the unit under test or simple domain values.
- Stub only calls needed by the scenario. Do not reset mocks manually.
- Verify meaningful side effects, captured commands, ownership boundaries, and calls that must not occur. Avoid mirroring every internal call.
- Use `ArgumentCaptor` when command composition or JWT-derived identity is observable behavior.
- Use Reactor `StepVerifier` for `Mono` behavior; do not block merely to simplify an assertion.

## Avoid low-value tests

Do not test Lombok-generated accessors, trivial records, mocks themselves, framework transaction behavior, or generic exception propagation unless a documented regression depends on it. Do not enable parallel execution or add test tags as part of ordinary unit-test work; Maven already separates `*Test` and `*IT` phases.

## Validation commands

On Windows, prefer the Maven wrapper:

```powershell
.\mvnw.cmd -Dtest=CreateProductTest test
.\mvnw.cmd test
.\mvnw.cmd clean test jacoco:report jacoco:check
```

Use the equivalent `./mvnw` commands on POSIX. Run PIT deliberately for important domain or application behavior, not after every test edit.

## Completion evidence

Report behaviors covered, test files changed, focused and broader commands run, results, and important scenarios intentionally left to integration tests.
