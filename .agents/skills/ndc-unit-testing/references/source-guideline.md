---
name: ndc-unit-testing
description: Evaluate unit test quality for NDC/Offer Java services against the NDC Unit Testing and Mocking Guideline. Checks naming convention compliance, @Nested usage, mocking discipline, over-mocking, primary input object mocking, domain object mocking, verify-heavy tests, and test structure smells. NON-SCORING by default — only findings that map to an already published MCP/Playbook standard may be promoted by the main evaluation prompt. All NDC-specific extensions remain advisory until formally published in the Engineering Playbook and indexed in MCP.
license: Apache-2.0
metadata:
  author: ndc-offer-domain
  version: "1.5"
compatibility: Java 21, JUnit 5, Mockito, AssertJ. Spring Boot backend services in NDC/Offer domain.
---

# Skill — NDC Unit Testing and Mocking Guideline (v1.5)

## Status

**Domain-approved, pending Playbook formalization.**

This skill reflects the NDC/Offer Unit Testing and Mocking Guideline — approved within the NDC/Offer domain but not yet published in the Engineering Playbook or indexed in MCP.

Therefore:

- Findings from this skill are `Evaluator-proposed` by default.
- Findings MUST NOT affect scoring directly.
- Findings MUST NOT be presented as Playbook violations.
- Findings are reported as maintainability observations, test-design risks, false-positive risks, and team alignment opportunities.

Only findings that map to an already published MCP/Playbook standard may be promoted by the main evaluation prompt.

Currently, the only known MCP-covered subset is:

- `docs/05-testing-and-quality/unit-testing-backend.md` → test naming convention `should<Outcome>When<Context>()`

All NDC-specific extensions remain non-scoring until published in the Engineering Playbook and indexed in MCP:

- `@Nested` usage rules
- mocking discipline
- primary input object mocking
- domain object mocking
- verify-heavy tests
- over-mocking detection
- test structure smells

High risk does not mean scoring impact until the rule exists in Playbook/MCP.

When this guideline is formally published in the Playbook and indexed in MCP:

- Move relevant `readStandard` calls to the main evaluation prompt.
- Reclassify findings as `MCP-backed` only when the exact standard is read and listed in `MCP Standards Used`.
- Retire or update this skill accordingly.

---

## MCP Discovery Result

| Topic | MCP Coverage | Relation to this skill |
|---|---|---|
| `unit-testing-backend.md` naming convention | EXPLICIT (`should<Outcome>When<Context>()`) | This skill aligns with the existing MCP naming rule |
| NDC mocking guideline | NOT IN MCP | Advisory only |
| Mock vs stub vs fake | NOT COVERED | Advisory only |
| Over-mocking detection | NOT COVERED | Advisory only |
| Primary input object mocking | NOT COVERED | Advisory only |
| Domain object mocking | NOT COVERED | Advisory only |
| Verify-heavy tests | NOT COVERED | Advisory only |
| `@Nested` class usage | NOT COVERED | Advisory only |

**Alignment note:** This skill aligns with `unit-testing-backend.md` on naming. The `@Nested` structure and mocking rules are NDC extensions, not replacements.

---

## Purpose

Evaluate unit test quality for NDC/Offer backend Java services against the NDC mocking and testing guideline.

Detect:

- naming convention compliance
- `@Nested` class usage
- mocking discipline
- over-mocking
- primary input object mocking
- domain object mocking
- verify-heavy tests
- test structure smells

This skill complements — and does NOT override — MCP standard evaluation.

---

## Scope

Evaluate test code under `src/test/java/unit/` or equivalent unit-test paths for:

1. Naming convention compliance
2. `@Nested` class usage — when to use and when not to
3. Mocking discipline — what to mock and what not to mock
4. Primary input object mocking — false-positive risk
5. Verify-heavy tests
6. Domain object mocking
7. Test structure smells

---

## Precondition Gate

This skill MUST only run if:

- Unit test source files exist under `src/test/java/unit/` or equivalent
- JUnit 5 and Mockito are present in `pom.xml`

If no unit tests are present → mark skill as `Unverified` and skip.

If Mockito is not present → skip mocking-specific rules and report them as `Unverified`.

---

## Output Format

Add under `## Additional Quality Insights`:

```markdown
### NDC Unit Testing and Mocking Findings
```

Table:

| Finding | Class / Method | Evidence | Recommendation | Risk | Confidence | Score Impact | Formalization Status |
|---|---|---|---|---|---|---|---|

Column definitions:

- **Risk**: `LOW` / `MEDIUM` / `HIGH`
- **Confidence**:
    - `HIGH` = direct code evidence from inspected test class/method
    - `MEDIUM` = inferred from representative pattern
    - `LOW` = speculative, limited sample, or code not fully inspected
- **Score Impact**:
    - `NO` = default for all NDC-specific findings
    - `MCP Candidate` = only for findings that map to an existing MCP standard, currently naming convention
    - `See Design Quality` = only if the main evaluation prompt promoted the finding after reading the MCP standard
- **Formalization Status**:
    - `MCP-covered` = already exists in MCP/Playbook
    - `Pending Playbook formalization` = NDC-specific extension
    - `Advisory` = quality suggestion only

`MCP Candidate` does not affect score by itself. It only signals that the finding may be reviewed by the main evaluation prompt because it maps to an existing MCP standard.

For every rule executed by this skill, the output MUST include a class compliance list.

For mandatory scans, the list MUST be repository-wide.

For non-mandatory checks, the list may be based on the inspected scope, but the inspected scope MUST be stated explicitly.

Required format:

```markdown
#### Compliance List — <Rule Name>

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

Rules that MUST list non-compliant classes with reason:

- Naming convention
- `@Nested` class usage
- Mocking discipline
- Primary input object mocking
- Domain object mocking
- Verify-heavy tests
- Over-mocking / excessive mocks
- Test structure smells

For each non-compliant class, `Why / Why not` MUST explain the specific best-practice condition that is not met.

---

## Reporting Discipline

- Do NOT report more than 2 detailed rows per category unless Risk = HIGH.
- Prioritize: `HIGH` → `MEDIUM` → `LOW`.
- HIGH-risk findings MUST NOT be suppressed.
- Mandatory scans MUST NOT use sampling.
- For mandatory scans, the evaluator MUST list every class that does not comply with the skill condition.
- For each non-compliant class, the evaluator MUST explain why it does not comply.
- Group similar findings with recurrence count, but still include the complete non-compliant class list:

```text
Same pattern found in 4 additional classes: ClassA, ClassB, ClassC, ClassD
```

- If the full list is long, include:
    - total candidate class count,
    - total non-compliant class count,
    - representative detailed method-level examples,
    - complete non-compliant class list in a compact table or appendix.
- If total findings exceed 8 rows → suppress LOW findings and note count.
- Do NOT suppress HIGH or mandatory-scan findings because of row limits.
- Do NOT report style-only observations if higher-risk test-design findings are present.
- Do NOT duplicate issues already reported in Design Quality Findings.
- When sampling is allowed for non-mandatory checks, state sample size and selection basis.

### Class-level compliance reporting for all skill rules

For every rule covered by this skill, the evaluator MUST list the classes that do not comply and explain why they do not comply.

This applies to:

1. Naming convention
2. `@Nested` class usage
3. Mocking discipline
4. Primary input object mocking
5. Domain object mocking
6. Verify-heavy tests
7. Over-mocking / excessive mocks
8. Test structure smells

The evaluator MUST NOT only provide aggregate percentages or general statements.

For each rule, the output MUST include:

- inspection scope,
- number of candidate classes inspected,
- number of non-compliant classes,
- class names,
- reason why each class does not comply,
- concrete evidence,
- recommendation.

Required table:

```markdown
#### Compliance List — <Rule Name>

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

If a class has multiple violations of the same rule, summarize the repeated pattern and include representative method-level examples.

If a rule is repository-wide mandatory, sampling is invalid.

If a rule uses sampling, the evaluator MUST clearly state:

- sample size,
- selection basis,
- why repository-wide inspection was not required by the skill,
- confidence level.

The phrase `some classes do not comply` is not acceptable unless followed by the complete non-compliant class list for the inspected scope.


### Mandatory reporting for primary input object mocking

Primary input object mocking requires a repository-wide scan.

If primary input object mocking is detected, it MUST be reported even if the total findings exceed 8 rows.

The report MUST list every non-compliant class and explain why each one does not comply.

Reason: this pattern can create false positives by controlling getter return values that may differ from production behavior, especially for:

- `null` values
- empty collections
- default values
- generated OpenAPI model defaults
- domain invariants
- lazy computed values
- behavior hidden behind getters

---

## Promotion Boundary

This skill detects advisory findings.

The main evaluation prompt is the only place that may promote a finding to scoring impact.

Promotion is allowed only when ALL conditions are true:

1. The finding maps to an existing MCP/Playbook standard.
2. The exact standard was read through MCP or authorized fallback.
3. The exact standard path appears in `MCP Standards Used`.
4. Concrete test class/method evidence was inspected.
5. The affected framework sub-criterion is explicitly named.
6. The finding is moved to `Design Quality Findings` or the relevant scoring rationale by the main prompt.
7. The no-double-penalty rule is respected.

Currently, this means:

- Naming convention may become `MCP Candidate`.
- NDC mocking discipline remains non-scoring.
- Primary input object mocking remains non-scoring.
- `@Nested` usage remains non-scoring.
- Domain object mocking remains non-scoring.
- Verify-heavy tests remain non-scoring.
- Test structure smells remain non-scoring.

---

## Rules

---

### 1. Naming Convention

**Standard:** `docs/05-testing-and-quality/unit-testing-backend.md` (MCP-covered) + NDC extension for `@Nested`

#### 1.1 Flat test methods (no `@Nested`)

Required pattern: `should<Outcome>When<Context>()`

```java
// Correct
void shouldReturnResponseWhenClientCallSucceeds() { ... }
void shouldThrowExceptionWhenRepositoryFails() { ... }
void shouldReturnEmptyWhenNoCriteriaProvided() { ... }

// Partial — missing When context
void shouldMapMarketingFlight() { ... }

// Non-compliant — inverted pattern
void whenHasItems_ShouldMap() { ... }

// Partial — wrong connector
void shouldReturnMessageFromErrorMessage() { ... }

// Partial — underscore variant
void shouldEmpty_whenListIsEmpty() { ... }

// Non-compliant — generic name
void testSomething() { ... }
```

Classification table:

| Pattern | Verdict |
|---|---|
| `should<Outcome>When<Context>()` | Compliant |
| `should<Outcome>()` — no When | Partial |
| `when<X>_Should<Y>()` — inverted | Non-compliant |
| `should<X>From<Y>()` — wrong connector | Partial |
| `should<X>_when<Y>()` — underscore | Partial |
| `test*()`, `check*()`, generic names | Non-compliant |
| Typo e.g. `WhenWhen` | Minor defect |

**How to inspect:**

- Read surefire XML reports (`testcase name=` attributes), or
- Inspect source files directly.

Minimum sample:

- At least 10 methods across different layers when possible.
- At least 3 test classes when possible.
- If using a larger automated sample, report total sampled methods and compliance percentages.

Classification:

- Risk = MEDIUM if representative sample shows <80% compliant
- Risk = LOW if representative sample shows 80–95% compliant
- Score Impact = NO by default
- Score Impact = MCP Candidate only when:
    1. `unit-testing-backend.md` was read through MCP,
    2. representative test evidence was inspected,
    3. systematic non-compliance is found,
    4. the main prompt promotes it.
- Formalization Status = MCP-covered

#### 1.2 `@Nested` class methods

Methods inside `@Nested` classes MUST also follow `should<Outcome>When<Context>()`.

The `@Nested` class name SHOULD be the public method name under test (PascalCase, no prefix):

```java
// Correct
@Nested
class CreateOffer {
    @Test
    void shouldReturnOfferWhenCriteriaIsValid() { ... }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() { ... }
}

// Non-compliant nested class name
@Nested
class TestCreateOffer { ... }        // unnecessary prefix
class WhenCreatingOffer { ... }      // When-prefixed class name
class CreateOfferTests { ... }       // Tests suffix
```

Classification:

- Method naming inside `@Nested` follows MCP-covered naming rule → may be MCP Candidate.
- Nested class naming itself is NDC-specific → Score Impact = NO.
- Formalization Status:
    - method naming = MCP-covered
    - nested class naming = Pending Playbook formalization

---

#### 1.3 Required naming output

The naming convention check MUST list non-compliant classes.

If using automated method-name analysis, aggregate percentages are allowed only as summary.

The output MUST also include:

```markdown
#### Compliance List — Naming Convention

Inspection scope: <repository-wide | sampled>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

Examples of `Why / Why not`:

- "Contains test methods that do not follow `should<Outcome>When<Context>()`."
- "Uses `test*` naming."
- "Uses inverted `when<X>_Should<Y>` naming."
- "Uses partial `should<Outcome>()` names without context."


---

### 2. `@Nested` Class Usage

**NDC guideline:** Use `@Nested` when it adds clarity. Do NOT use it to add structure for its own sake.

This section is NDC-specific and not currently MCP-covered.

**Neutrality requirement:** Do NOT interpret `@Nested` presence as positive or negative. Report the count and usage pattern only. Do not state that `@Nested` usage "indicates well-organized tests", "is generally acceptable", or any equivalent positive or negative judgment. The main evaluation prompt interprets the pattern. This skill reports the evidence.

#### When to use `@Nested`

Use `@Nested` when ALL of the following are true:

- The public method under test has **3 or more** test scenarios
- The scenarios have **meaningfully different contexts** that benefit from grouping
- The scenarios benefit from **shared setup** (`@BeforeEach` inside `@Nested`) specific to that method

```java
// Correct use — 3+ scenarios with distinct contexts
@Nested
class ProcessOffer {
    @BeforeEach
    void setup() { /* shared setup for all processOffer scenarios */ }

    @Test
    void shouldReturnOfferWhenAncillariesAvailable() { ... }

    @Test
    void shouldReturnEmptyWhenNoAncillariesFound() { ... }

    @Test
    void shouldThrowExceptionWhenProviderFails() { ... }
}
```

#### When NOT to use `@Nested`

```java
// Unnecessary @Nested — only 1 test scenario
@Nested
class GetOfferId {
    @Test
    void shouldReturnFirstOfferIdWhenOffersExist() { ... }
}

// Unnecessary @Nested — no distinct shared setup
@Nested
class ValidateInput {
    @Test
    void shouldReturnTrueWhenInputIsValid() { ... }

    @Test
    void shouldReturnFalseWhenInputIsNull() { ... }
    // These 2 tests have no shared setup — flat structure is cleaner
}
```

Classification:

| Observation | Risk | Score Impact | Formalization Status |
|---|---|---|---|
| `@Nested` with 1 test method | LOW | NO | Pending Playbook formalization |
| `@Nested` with 2 tests and no shared `@BeforeEach` | LOW | NO | Pending Playbook formalization |
| `@Nested` with non-compliant class name | MEDIUM | NO | Pending Playbook formalization |
| Missing `@Nested` for method with 5+ tests (flat, hard to read) | LOW | NO | Pending Playbook formalization |

---

#### 2.1 Required `@Nested` output

The `@Nested` usage check MUST list non-compliant classes when inspected.

The output MUST include why each class does not comply, for example:

- `@Nested` class has only one test method.
- `@Nested` class has two tests but no distinct shared setup.
- `@Nested` class name does not match the method under test.
- `@Nested` class name uses unnecessary prefixes/suffixes such as `Test`, `When`, or `Tests`.

Required table:

```markdown
#### Compliance List — @Nested Usage

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```


---

### 3. Mocking Discipline

**NDC guideline core principle:** Mock only collaborator dependencies that are external to the behavior under test.

This section is NDC-specific and not currently MCP-covered.

#### 3.1 What MUST be mocked

Mock these when they are direct collaborators of the unit under test:

- Repositories (`*Repository`)
- HTTP/Feign/gRPC clients (`*Client`, `*RestClient`)
- Messaging producers/consumers
- `Clock`, UUID generators, randomizers
- External adapters and gateways
- Outbound ports (hexagonal) or infrastructure adapters (three-layer)

#### 3.2 What MUST NOT be mocked

Instantiate as real objects:

- Domain entities and value objects
- Java records used as model objects
- Commands, queries, input/output DTOs
- Result objects
- Pure mappers and side-effect-free utilities
- Collections and helper data structures
- Simple validation inputs and fixtures

```java
// Correct — mock only infrastructure
@Mock
private AncillariesRepository ancillariesRepository;

@Mock
private SpecialNeedsClient specialNeedsClient;

// Real objects — no @Mock
private OfferCriteria offerCriteria = new OfferCriteria(...);
private OfferQuery offerQuery = new OfferQuery(...);

// Over-mocking — domain objects should be real
@Mock
private OfferCriteria offerCriteria;    // domain record, should be real

@Mock
private OfferQuery offerQuery;          // domain record, should be real

@Mock
private OfferItemMapper mapper;         // pure mapper, should be real unless it is an architectural boundary/collaborator in the unit under test
```

**How to inspect:**

- Read `@Mock` field declarations in test classes.
- For each mock, classify:
    - infrastructure collaborator
    - outbound port
    - external dependency
    - domain object
    - DTO/query/command/result
    - pure mapper
    - simple utility

Classification:

| Evidence | Risk | Score Impact | Formalization Status |
|---|---|---|---|
| Domain records or value objects annotated with `@Mock` | HIGH | NO | Pending Playbook formalization |
| Pure mappers with no side effects annotated with `@Mock` | MEDIUM | NO | Pending Playbook formalization |
| Simple DTOs or result objects annotated with `@Mock` | MEDIUM | NO | Pending Playbook formalization |
| Infrastructure correctly mocked (repositories, clients, ports) | Positive signal | NO | Advisory |

Note:

- HIGH risk means test-design risk, not scoring impact.
- These findings are strong candidates for future Playbook formalization.
- Until formalized, they remain Additional Quality Insights.

---

#### 3.3 Required mocking discipline output

The mocking discipline check MUST list non-compliant classes when inspected.

The output MUST explain why each class does not comply, for example:

- domain entity/value object is mocked,
- command/query/DTO/result object is mocked,
- pure mapper is mocked when it should be a real object,
- simple utility/helper data structure is mocked,
- too many mocks hide the behavior under test.

Required table:

```markdown
#### Compliance List — Mocking Discipline

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

---

#### 3.4 `@Mock` field grep inspection (MANDATORY, repository-wide)

Perform a repository-wide grep scan of all test classes for `@Mock` field declarations.

For each `@Mock` field found:

- Verify the field is referenced in at least one test method via `when(...)`, `verify(...)`, or direct invocation through the unit under test.
- If a `@Mock` field is declared but never referenced in any test method of the same class, classify it as an **unused mock field** — advisory finding, risk MEDIUM.
- If a `@Mock` field is used on a primary input object (see §4), cross-reference the finding with §4 and consolidate. Do not double-report.

**Required output when unused mocks are found:**

```markdown
#### Compliance List — Unused @Mock Fields

Inspection scope: repository-wide
Test classes scanned: <N>
Classes with unused @Mock fields: <N>

| Class | Unused @Mock Field | Evidence | Recommendation |
|---|---|---|---|
| `<class>` | `@Mock <Type> <fieldName>` | Field declared but not referenced in any test method | Remove or use the mock; if intentional, add a comment explaining why |
```

**Required output when none found:**

```
Repository-wide @Mock field scan completed — no unused @Mock fields detected.
```

Classification:

| Finding | Risk | Score Impact | Formalization Status |
|---|---|---|---|
| Unused `@Mock` field declared but never referenced | MEDIUM | NO | Pending Playbook formalization |
| `@Mock` field on primary input object (cross-reference §4) | HIGH | NO | Pending Playbook formalization |

Score Impact = NO. Advisory only.

---

### 4. Primary Input Object Mocking — False Positive Risk

**NDC guideline core principle:** Mock dependencies. Build data. Do not mock the object the unit under test is supposed to understand.

When testing mappers, assemblers, converters, validators, domain services, or application services, the primary input object under test SHOULD be a real object, not a Mockito mock.

Mocking the primary input object can create false positives because the test controls getter return values that may differ from production behavior, especially:

- `null` handling
- empty collection handling
- default generated-model values
- domain invariant enforcement
- mutable collection behavior
- derived/computed getter behavior
- optional-field behavior
- generated OpenAPI model defaults

This is especially risky for mapper tests where the mapper reads a source object and transforms it into an output object.

#### 4.1 What is a primary input object?

A primary input object is the object passed directly to the method under test, or the object whose state is the main behavior being inspected.

Examples:

```java
// source is the primary input object
final var result = mapper.toResponse(source);

// query is the primary input object
final var result = useCase.execute(query);

// event is the primary input object
handler.handle(event);
```

#### 4.2 Bad pattern — mocked source object

```java
@Mock
private FarePriceSummary source;

@Test
void mapElement() {
    when(source.getDiscounts()).thenReturn(List.of(discount));
    when(source.getEquivAmount()).thenReturn(Price.of(BigDecimal.TEN, "EUR"));

    final var result = testSubject.toResponse(source);

    assertThat(result.getEquivAmount().getAmount()).isEqualByComparingTo(BigDecimal.TEN);
}
```

Why this is risky:

- The test forces `source.getEquivAmount()` to return a non-null value.
- If production `source.getEquivAmount()` can return `null`, this test will not catch a possible `NullPointerException`.
- The test validates the mock setup more than the mapper behavior.
- The test bypasses real object construction and domain invariants.
- The test may hide default/null semantics of generated models or domain objects.

#### 4.3 Better pattern — real input object

```java
@Test
void shouldMapFareDetailPriceWhenFarePriceSummaryHasValues() {
    final var source = FarePriceSummaryMother.withEquivAmount(BigDecimal.TEN, "EUR");

    final var result = testSubject.toResponse(source);

    assertThat(result.getEquivAmount())
        .extracting(
            FareDetailPriceAllOfEquivAmount::getAmount,
            FareDetailPriceAllOfEquivAmount::getCurrency)
        .containsExactly(BigDecimal.TEN, "EUR");
}
```

#### 4.4 Better pattern — explicit nullability case

```java
@Test
void shouldMapFareDetailPriceWhenEquivAmountIsNull() {
    final var source = FarePriceSummaryMother.withoutEquivAmount();

    final var result = testSubject.toResponse(source);

    assertThat(result.getEquivAmount()).isNull();
}
```

#### 4.5 Recommended alternatives

Use one of:

- real constructors
- static factories
- Test Data Builders
- Object Mothers
- fixture factories
- generated test fixtures
- fakes for complex collaborators

Do not use Mockito to create the primary data object unless one of the documented exceptions applies.

#### 4.6 Valid exceptions

Mocking the primary input object MAY be acceptable when:

- the input type is a framework proxy that cannot be safely instantiated
- the input type is generated and extremely expensive to construct, and no builder/fixture exists yet
- the test validates a branch that cannot be reached through stable object construction
- the input is not data but an actual collaborator dependency
- the test is explicitly an interaction test and interaction is the behavior under test

When using an exception, the test SHOULD document it with a short comment.

Example:

```java
// Intentional mock: generated object is expensive to construct and this test only validates delegation.
```

#### 4.7 Detection heuristics

Flag a test when ALL of the following are true:

1. A field or local variable is annotated/created as a Mockito mock:
    - `@Mock private <Type> source;`
    - `final var source = mock(<Type>.class);`
    - `var input = mock(<Type>.class);`
2. That mock is passed directly to the method under test:
    - `testSubject.map(source)`
    - `testSubject.toResponse(source)`
    - `service.execute(query)`
    - `handler.handle(event)`
3. The test stubs getters or simple accessors on that mock:
    - `when(source.getX()).thenReturn(...)`
    - `when(query.getX()).thenReturn(...)`
    - `when(event.getX()).thenReturn(...)`

Increase confidence when:

- the mocked type is in a domain package
- the mocked type is a DTO/query/command/result/model object
- the class under test is a mapper/converter/assembler
- the assertions compare the result to mocked objects by identity
- no null/empty/default-value test exists for the same mapping path

#### 4.8 Repository-wide mandatory scan

The primary input object mocking inspection MUST NOT be limited to representative samples.

When this skill is enabled, the evaluator MUST run a repository-wide scan across all unit mapper tests under:

```text
src/test/java/unit/**/*MapperTest.java
```

If the project uses an equivalent unit-test path, include that path and state it explicitly.

The evaluator MUST inspect every candidate test class where any of these patterns appear:

```java
@Mock private <Type> source;
@Mock private <Type> query;
@Mock private <Type> event;
mock(<Type>.class)
when(<object>.getX()).thenReturn(...)
when(<object>.x()).thenReturn(...)
```

A class is non-compliant when direct evidence shows ALL of the following:

1. A primary input object is mocked.
2. The mocked object is passed directly to the mapper, converter, assembler, service, handler, or test subject method.
3. Getters or simple accessors on that mocked object are stubbed.

Examples of method calls to inspect:

```java
testSubject.map(source)
testSubject.toResponse(source)
testSubject.toDto(source)
testSubject.toDomain(source)
mapper.map(source)
service.execute(query)
handler.handle(event)
```

The output MUST state one of:

```text
Repository-wide scan completed; primary input object mocking detected.
```

or:

```text
Repository-wide scan completed; no primary input object mocking detected.
```

The phrase `inspected representative samples` is NOT acceptable for this mandatory check.

When detected, the output MUST include:

- total number of candidate `*MapperTest.java` files scanned,
- total number of non-compliant classes,
- complete list of non-compliant classes,
- why each class does not comply,
- representative examples with file, class, method, mocked variable, stubbings, and method-under-test call.

Required compact table for non-compliant classes:

| Class | Why it does not comply | Mocked primary input | Stubbed accessors | Method under test | Recommendation |
|---|---|---|---|---|---|

Classification when detected:

- Risk = HIGH
- Confidence = HIGH when direct file/class/method evidence exists
- Score Impact = NO
- Formalization Status = Pending Playbook formalization

This finding remains non-scoring until the rule is formally published in the Engineering Playbook and indexed in MCP.

#### 4.9 Detection examples from NDC/Offer

Example pattern:

```java
@Mock
private FarePriceSummary source;

@Test
void mapElement() {
    final var price = mock(Price.class);
    when(priceMapper.toResponse(source.getSaleFare())).thenReturn(price);

    when(source.getDiscounts()).thenReturn(List.of(discount));
    when(source.getEquivAmount()).thenReturn(Price.of(BigDecimal.TEN, "EUR"));

    final var result = testSubject.toResponse(source);

    assertThat(result.getBaseAmount()).isEqualTo(price);
}
```

Finding to report:

```text
Primary mapper input object is mocked. `FarePriceSummary source` is a Mockito mock and its getters are stubbed before passing it to `testSubject.toResponse(source)`. This can hide nullability/default-value behavior and generate false positives.
```

Recommended remediation:

```text
Build a real `FarePriceSummary` using a Test Data Builder or Object Mother. Add explicit tests for null `equivAmount`, empty discounts, and any optional generated-model fields.
```

#### 4.10 Classification

| Evidence | Risk | Confidence | Score Impact | Formalization Status |
|---|---|---|---|---|
| Primary mapper/converter/assembler input object mocked and getter-stubbed | HIGH | HIGH when direct code evidence exists | NO | Pending Playbook formalization |
| Primary service/use-case command/query/event mocked and getter-stubbed | HIGH | HIGH when direct code evidence exists | NO | Pending Playbook formalization |
| Mocked getter controls nullable value with no nullability test nearby | HIGH | MEDIUM/HIGH depending on evidence | NO | Pending Playbook formalization |
| Mocked source object used only for interaction/delegation and exception documented | LOW | MEDIUM | NO | Advisory |
| Real input object built with builder/mother/fixture | Positive signal | HIGH | NO | Advisory |

Note:

- HIGH risk means false-positive/test-design risk, not scoring impact.
- This finding MUST be reported when detected, because it has been observed as a recurring team gap.
- Until formalized, it remains Additional Quality Insights.

---

### 5. Verify-Heavy Tests

**NDC guideline:** Prefer asserting returned values, thrown exceptions, or state changes over verifying interactions. `verify(...)` is only appropriate when interaction IS the behavior under test.

This section is NDC-specific and not currently MCP-covered.

**Smell:** a test that has more `verify(...)` calls than meaningful assertions on outcomes.

```java
// Verify-heavy — tests wiring, not behavior
@Test
void shouldCallRepositoryWhenProcessing() {
    service.process(query);
    verify(repository).findById(any());
    verify(repository).save(any());
    verify(mapper).toDto(any());
    // No assertion on the returned result or thrown exception
}

// Behavior-focused
@Test
void shouldReturnOfferWhenProcessingSucceeds() {
    when(repository.findById(any())).thenReturn(Optional.of(offer));
    final OfferResponse result = service.process(query);
    assertThat(result.offerId()).isEqualTo(expectedId);
    assertThat(result.price()).isEqualTo(expectedPrice);
}
```

**How to inspect:**

- Count `verify(...)` calls vs `assertThat(...)` / `assertEquals` / `assertThrows` calls per test method.
- If verify > assertions in majority of tests → report a finding.
- Do not flag verify-only tests when interaction is explicitly the behavior under test (e.g., event publisher, command dispatcher, message sender).

Classification:

| Evidence | Risk | Score Impact | Formalization Status |
|---|---|---|---|
| verify() > assertions in >50% of test methods inspected | HIGH | NO | Pending Playbook formalization |
| verify() > assertions in 20–50% of test methods | MEDIUM | NO | Pending Playbook formalization |
| Occasional verify-only tests | LOW | NO | Advisory |

Note:

- HIGH risk means behavior-validation risk, not scoring impact.
- These findings are candidates for future Playbook formalization only.

---

#### 5.1 Required verify-heavy output

The verify-heavy test check MUST list non-compliant classes when inspected.

The output MUST explain why each class does not comply, for example:

- more `verify(...)` calls than meaningful assertions,
- interaction is verified when returned behavior should be asserted,
- test validates wiring rather than observable behavior.

Required table:

```markdown
#### Compliance List — Verify-Heavy Tests

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```


---

### 6. Test Structure Smells

Detect additional smells from the NDC guideline.

This section is NDC-specific and not currently MCP-covered.

| Smell | Evidence | Risk | Score Impact | Formalization Status |
|---|---|---|---|---|
| More `@Mock` fields than meaningful assertions in average test | Count @Mock vs assertThat per class | MEDIUM | NO | Pending Playbook formalization |
| Unused `@Mock` fields declared but never used in tests | `@Mock` field with no `when(...)` and no `verify(...)` | LOW | NO | Advisory |
| Missing `@ExtendWith(MockitoExtension.class)` when using `@Mock` | Class annotation absent and no alternative initialization | MEDIUM | NO | Pending Playbook formalization |
| Wildcard imports in test classes | `import org.mockito.*` or similar | LOW | NO | Advisory |
| Non-final local variables where `final` is possible | Style only | LOW | NO | Advisory |
| `@InjectMocks` used on classes that don't need injection | Overuse of DI in tests | LOW | NO | Advisory |

Missing `MockitoExtension` may indicate real reliability risk, but it remains non-scoring until covered by Playbook/MCP or by a failing test artifact.

---

### 6.1 Required test-structure output

The test-structure smell check MUST list non-compliant classes when inspected.

The output MUST explain why each class does not comply, for example:

- more `@Mock` fields than meaningful assertions,
- unused `@Mock` fields,
- missing `@ExtendWith(MockitoExtension.class)` while using `@Mock`,
- wildcard imports,
- unnecessary `@InjectMocks`,
- excessive setup that hides test intent.

Required table:

```markdown
#### Compliance List — Test Structure Smells

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```


---

## Design Quality Deduplication

If a naming finding in this skill was already reported in `Design Quality Findings` or the main evaluation testing section:

- Do NOT duplicate.
- Reference the existing finding:

```text
See Design Quality → Tests inspection
```

- Only add mocking-specific detail not covered by the main evaluation.

If an NDC-specific finding overlaps with an MCP-backed finding:

- Keep the MCP-backed finding in Design Quality.
- Keep this skill's finding as a supporting non-scoring insight only if it adds actionable test-level detail.

---

## Playbook Integration Notes

This skill is designed to run within the Iberia Engineering Copilot evaluation platform.

### Placement in the evaluation workflow

This skill executes AFTER:

1. MCP discovery (`searchStandards` + `readStandard`)
2. Architecture validation
3. Design Quality analysis
4. Test execution evidence collection

This skill MUST NOT:

- run before MCP discovery is complete
- override or reinterpret MCP standards
- introduce new scoring rules
- affect architecture compliance conclusions
- classify findings as MCP-backed directly
- change scores directly

### Relevant playbook paths

| Path | Relevance |
|------|-----------|
| `docs/05-testing-and-quality/unit-testing-backend.md` | Naming convention only |
| `docs/05-testing-and-quality/integration-testing.md` | Integration testing context; not used by this skill directly |
| `docs/05-testing-and-quality/jacoco.md` | Coverage thresholds; not used by this skill directly |
| `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` | Ports/adapters context; not a mocking guideline |
| `docs/03-tech-stack/java-style-guide.md` | General Java style; not a mocking guideline |

### execution-config.yml activation

To activate this skill, add to `execution-config.yml`:

```yaml
skills:
  - ndc-unit-testing
```

Mapped file: `docs/skills/ndc-unit-testing-skill.md`

---

## Hard Rules

- Findings are Evaluator-proposed by default.
- Do NOT affect scoring directly.
- Only the subset already covered by MCP/Playbook may be marked as `MCP Candidate`; currently this is unit test naming from `unit-testing-backend.md`.
- Do NOT present findings as Playbook violations unless the exact MCP standard was read and listed by the main prompt.
- Do NOT recommend patterns not present in this guideline.
- Findings MUST include concrete class/method evidence.
- For every skill rule executed, the evaluator MUST list non-compliant classes and explain why each class does not comply.
- Aggregate counts or percentages are not enough without class-level non-compliance details.
- Mandatory scans MUST be repository-wide and MUST include complete non-compliant class lists.
- For non-mandatory sampled checks, the evaluator MUST state sample size, selection basis, and confidence.
- If test code was not inspected → mark as `Unverified`.
- Risk level does not imply scoring impact.
- HIGH risk findings remain non-scoring when the rule is NDC-specific and not yet published in Playbook/MCP.
- Primary input object mocking MUST be reported when direct evidence is found.
- Repository-wide primary input object mocking scan MUST inspect all `src/test/java/unit/**/*MapperTest.java` files or equivalent unit mapper test paths.
- Representative sampling is invalid for primary input object mocking.
- The report MUST state whether repository-wide primary input object mocking was detected or not detected.
- If detected, the report MUST list every non-compliant class found by the scan.
- If detected, the report MUST explain why each listed class does not comply.
- If detected, representative examples MUST include method-level evidence: mocked primary input variable, stubbed accessor, and method-under-test call.
- Do NOT mark primary input object mocking as scoring impact until the rule is formalized in Playbook/MCP.
- When this guideline is published in the Playbook → update classification to MCP-backed where applicable and retire/update this skill.