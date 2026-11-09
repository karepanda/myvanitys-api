---
name: mapping-between-boundaries
description: Analyze how data models are shared or mapped across architectural boundaries (web, application, domain, persistence). Detects the mapping strategy used per boundary and per operation type, assesses whether the strategy appears intentional and reasonable for the declared architecture, and identifies mapper code smells. NON-SCORING by default — findings must not affect evaluation score unless the main evaluation prompt promotes them using MCP/Playbook evidence or authorized fallback. Applicable to three-layer, hexagonal, and vertical slice architectures.
license: Apache-2.0
metadata:
  author: iberia-software-engineering
  version: "1.4"
  inspired_by: "Get Your Hands Dirty on Clean Architecture — Chapter 9, Mapping Between Boundaries"
compatibility: Java 21 LTS. Spring Boot backend services. Three-layer, hexagonal, vertical slice.
---

# Skill — Mapping Between Boundaries (v1.4)

## Status

Evaluator extension. NON-SCORING by default.

This skill is not backed by a current MCP standard.
The Engineering Playbook does not define explicit mapping strategy rules.

Therefore:

- Findings from this skill are `Evaluator-proposed` by default.
- Findings MUST NOT affect scoring directly.
- Findings MUST NOT be presented as Playbook violations.
- Findings are reported as architectural observations, boundary-coupling risks, mapper responsibility risks, and improvement opportunities.

This skill detects boundary mapping risk.
It does not define architecture compliance.

When a mapping strategy standard is published in the Playbook and indexed in MCP:

- Move relevant `readStandard` calls to the main evaluation prompt.
- Reclassify findings as `MCP-backed` only when the exact standard is read and listed in `MCP Standards Used`.
- Retire or update this skill accordingly.

---

## MCP Discovery Result

| Topic | MCP Coverage | Relation to this skill |
|---|---|---|
| Mapping strategies (No Mapping, Two-Way, Full, One-Way) | NOT COVERED | Advisory only |
| Mapper responsibility (transform only) | POTENTIALLY RELATED — architecture-conditional: `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` when `expected_architecture: hexagonal`; `docs/07-architecture-and-patterns/55-vertical-slice-architecture.md` when `expected_architecture: vertical-slice` | Requires main-prompt MCP validation with the correct architecture path |
| DTO boundaries | POTENTIALLY RELATED to architecture standards and framework Layer Decoupling sub-criteria — use architecture-specific standard path from `mcp-standards--<EXECUTION_DATE>.json` | Requires main-prompt MCP validation |
| Command objects per use case | NOT COVERED | Advisory only |
| Domain model framework independence | POTENTIALLY RELATED — architecture-conditional: hexagonal architecture standard when hexagonal; `docs/07-architecture-and-patterns/55-vertical-slice-architecture.md` when vertical-slice | Requires main-prompt MCP validation with the correct architecture path |
| Framework annotations in domain | POTENTIALLY RELATED — architecture-conditional: use the architecture standard matching `expected_architecture` in `execution-config.yml` | Requires main-prompt MCP validation with the correct architecture path |
| Business logic in mappers (VSA cross-slice) | POTENTIALLY RELATED to `docs/07-architecture-and-patterns/55-vertical-slice-architecture.md` when `expected_architecture: vertical-slice` | Requires main-prompt MCP validation |

**Governance note:**

`POTENTIALLY RELATED` does not mean MCP-backed.

A finding from this skill can only become MCP-backed if the main evaluation prompt:

1. reads the exact MCP standard,
2. lists the exact standard path in `MCP Standards Used`,
3. validates concrete class/method evidence,
4. maps the finding to a framework sub-criterion,
5. applies the no-double-penalty rule.

---

## Purpose

Evaluate how the service handles data models across architectural boundaries.

This skill does three things:

1. **Detect** — what mapping strategy the service uses per boundary and per operation type.
2. **Assess** — whether the detected strategy appears intentional, consistent, and reasonable for the declared architecture.
3. **Find smells** — mapper code smells that may indicate misplaced responsibility.

Key principle: mapping is a trade-off.

- Less mapping → higher coupling.
- More mapping → higher complexity.

The goal is not to maximize or minimize mapping.
The goal is to apply mapping intentionally and consistently.

---

## Scope

Inspect production code under `src/main/java/`.

Focus on:

- Controller classes and their mappers
- Application service / use case classes
- Domain model classes
- Persistence layer classes and their mappers
- Any class with `Mapper`, `Converter`, `Assembler`, or `Transformer` in the name
- MapStruct interfaces and abstract mappers
- Generated OpenAPI model usage at boundaries

---

## Precondition Gate

This skill MUST only run if:

- Java source files are present under `src/main/java/`
- At least one architectural boundary is detectable:
  - controller
  - application service
  - use case
  - domain package
  - repository
  - adapter
  - mapper/converter/assembler

If no architectural boundaries are detectable → report as `Unverified` and note absence.

Note: absence of mappers is itself a signal. It may indicate a No Mapping strategy. Do NOT skip analysis only because mappers are absent.

---

## Output Format

Add under `## Additional Quality Insights`:

```markdown
### Mapping Between Boundaries Findings
```

### Section A — Strategy Map (always present)

```markdown
#### Detected Mapping Strategies

| Boundary | Operation | Strategy Detected | Assessment | Notes | Score Impact | Formalization Status |
|---|---|---|---|---|---|---|
| Web → Application | Command | Two-Way | Reasonable | Request DTO mapped before use case call | NO | Evaluator-proposed |
| Web → Application | Query | No Mapping | Review | Direct domain return observed | NO | Evaluator-proposed |
| Application → Domain | Command | No Mapping | Reasonable | Use case creates domain object directly | NO | Evaluator-proposed |
| Domain → Persistence | Any | Two-Way | Reasonable | Persistence entity mapper present | NO | Evaluator-proposed |

**Overall pattern:** Intentional mix / Accidental mix / Consistent strategy / Unverified
```

Assessment values:

- `Reasonable`
- `Review`
- `Risk`
- `Unverified`

Do NOT use `Compliant` or `Non-compliant` in this section unless the main prompt has promoted the finding using MCP/Playbook evidence.

### Section B — Mapper Smells (always present, even if empty)

```markdown
#### Mapper Code Smells

| Smell | Class / Method | Evidence | Risk | Recommendation | Score Impact | Formalization Status |
|---|---|---|---|---|---|---|
```

If no smells found → explicitly state:

```text
No mapper code smells detected in inspected classes.
```

### Section C — Compliance Lists by Rule (mandatory for executed checks)

For every rule/check executed by this skill, the output MUST list the classes that do not comply and explain why they do not comply.

Required format:

```markdown
#### Compliance List — <Rule Name>

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

This applies to all checks covered by this skill, including:

- Web → Application boundary mapping
- Application → Domain boundary mapping
- Domain → Persistence boundary mapping
- Mapper business logic
- Mapper service/repository/client calls
- Mapper validation
- Domain model framework annotation contamination
- Mapper side effects
- God mapper / bloated mapper
- Semantic leakage
- Mapping duplication

For each non-compliant class, `Why / Why not` MUST explain the specific best-practice condition that is not met.

Aggregate counts are allowed only as a summary.
They are not enough without class-level non-compliance details.

If a check is repository-wide, sampling is invalid.
If a check is sampled or targeted, the evaluator MUST state the inspected scope, sample size, selection basis, and confidence.

Score Impact values:

- `NO` = default for all findings from this skill
- `MCP Candidate` = potentially related to an existing MCP standard, requires main-prompt validation
- `See Design Quality` = only if the main prompt already promoted the finding

Formalization Status values:

- `Evaluator-proposed`
- `Candidate for MCP validation`
- `Pending Playbook formalization`
- `MCP-covered` only when explicitly validated by the main prompt

---

## Class-Level Compliance Reporting

For every rule executed by this skill, the evaluator MUST list the classes that do not comply and explain why each class does not comply.

The evaluator MUST NOT only provide general statements such as:

```text
mapper smells detected
boundary mapping risk observed
some mappers use collaborators
```

Those statements are incomplete unless followed by a class-level compliance list.

For each executed rule, include:

- inspection scope,
- candidate classes inspected,
- non-compliant class count,
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

If no class violates a rule, state:

```text
No non-compliant classes found for <Rule Name> in the inspected scope.
```

For HIGH-risk mapper smells, the inspection SHOULD be repository-wide across mapper-like classes unless the main prompt explicitly limits scope.

Mapper-like classes include files with these names:

```text
*Mapper.java
*Converter.java
*Assembler.java
*Transformer.java
```


---

## Promotion Boundary

This skill is advisory.

The main evaluation prompt is the only place that may promote a finding to scoring impact.

Promotion is allowed only when ALL conditions are true:

1. The finding maps to an existing MCP/Playbook standard or an explicitly authorized fallback standard.
2. The exact standard was read through MCP or approved fallback.
3. The exact standard path appears in `MCP Standards Used`.
4. Concrete class/method evidence was inspected.
5. The affected framework sub-criterion is explicitly named.
6. The finding is moved to `Design Quality Findings` or the relevant scoring rationale by the main prompt.
7. The no-double-penalty rule is respected.

This skill MUST NOT:

- change scores directly
- classify findings as `MCP-backed`
- declare architecture compliance/non-compliance
- reduce Layer Decoupling
- reduce Code Structure
- reduce Code Quality
- present advisory matrix results as Playbook rules

This skill MAY:

- mark findings as `MCP Candidate`
- mark findings as `Candidate for MCP validation`
- report HIGH/MEDIUM/LOW design risks
- provide concrete class/method evidence
- recommend follow-up validation by the main prompt

---

## Core Concepts

### The four mapping strategies

| Strategy | Description | Typical signal in code |
|---|---|---|
| **No Mapping** | Same model used across layers | Domain object returned from controller; `@Entity` in domain class |
| **Two-Way** | Each layer has its own model, mapped back and forth | `XxxMapper.toDto()` + `XxxMapper.toDomain()` present |
| **Full Mapping** | Use case-specific input/output models | `CreateOfferCommand`, `UpdateOfferCommand`, separate per-operation DTOs |
| **One-Way** | Shared read-only interface implemented by all layer models | Domain object implements `OfferState` interface used by web layer |

### The three boundaries to evaluate

```text
[ Web / Presentation ]
         ↕  boundary 1: web → application
[ Application / Use Cases ]
         ↕  boundary 2: application → domain
[ Domain / Model ]
         ↕  boundary 3: domain → persistence
[ Persistence / Infrastructure ]
```

### Operation types

Distinguish between:

- **Command** (write): creates, updates, deletes state — usually benefits from Full or Two-Way mapping at external boundaries.
- **Query** (read): retrieves state — may allow less mapping for pragmatism, depending on architecture and contract stability.

---

## Phase 1 — Strategy Detection (MANDATORY)

For each boundary, inspect the code and classify the strategy in use.

A service MAY use different strategies per boundary and per operation. This is normal and expected.

Document each combination found.

### Boundary 1: Web → Application

**Inspect:**

- Controller method signatures — what types do they receive and return?
- Are there dedicated command objects per use case?
- Are domain objects used directly as request/response?
- Are OpenAPI-generated DTOs used directly in application layer?
- Are generated OpenAPI models confined to infrastructure/web adapter packages?

**Classification signals:**

```text
No Mapping:    Controller returns domain object directly
               Domain object used as @RequestBody
               Application service returns domain object to controller
               OpenAPI-generated DTO passed into application/domain directly

Two-Way:       Controller maps request DTO → domain/command → response DTO
               XxxMapper.toDomain() and XxxMapper.toDto() both present

Full Mapping:  CreateXxxCommand, UpdateXxxCommand per operation
               Each endpoint has its own request/response model
               Use case port accepts operation-specific input type

One-Way:       Domain implements read-only interface used in controller
               Controller depends on XxxView or XxxState interface
```

### Boundary 2: Application → Domain

**Inspect:**

- Do application services pass raw request objects to domain?
- Does the domain model contain application-level fields?
- Are domain objects shared between multiple use cases without adaptation?
- Do domain methods accept infrastructure/web DTOs?

**Classification signals:**

```text
No Mapping:    Application service passes domain-specific values/entities directly
               Application creates domain objects from commands/queries
               Domain model is clean of use-case-specific infrastructure fields

Two-Way:       Application service maps to domain-specific objects
               Domain model is clean of external DTOs

Full Mapping:  Each use case creates a specific command/event for the domain
               Domain never sees application DTOs

One-Way:       Domain exposes state interface; application reads via interface

Risk signal:   Application passes web/persistence DTOs directly into domain
               Domain methods accept generated OpenAPI DTOs
               Domain imports infrastructure/web packages
```

### Boundary 3: Domain → Persistence

**Inspect:**

- Does the domain model have JPA annotations (`@Entity`, `@Table`, `@Column`)?
- Are there separate persistence entities that map to/from domain?
- Does the repository return domain objects or persistence entities?
- Does the domain import persistence packages?

**Classification signals:**

```text
No Mapping:    Domain class is annotated with @Entity
               Repository returns @Entity objects used directly as domain
               Domain has database-specific fields (e.g. optimistic lock version)

Two-Way:       Separate XxxEntity in persistence layer
               XxxEntityMapper.toDomain() / XxxEntityMapper.toEntity() present
               Repository implementation maps before returning

Full Mapping:  Per-operation persistence models (rare, usually overkill)

One-Way:       Domain implements persistence interface (uncommon in Spring)
```

---

### Required boundary output

For every boundary inspected, the evaluator MUST include a compliance list for classes that do not follow the detected/expected boundary mapping responsibility.

Required table:

```markdown
#### Compliance List — <Boundary Name>

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```


---

## Phase 2 — Advisory Appropriateness Assessment

After detecting the strategy per boundary, assess whether it appears reasonable.

Use the declared `expected_architecture` and the detected operation type.

This matrix is advisory.

It is NOT an MCP-backed standard.
It is NOT a Playbook compliance matrix.
It MUST NOT affect score directly.

### Advisory reference matrix

| Architecture | Boundary | Operation | Usually Reasonable | Often Acceptable | Review / Risk |
|---|---|---|---|---|
| Hexagonal | Web → App | Command | Full Mapping | Two-Way | No Mapping |
| Hexagonal | Web → App | Query | Two-Way | No Mapping when read-only and contract-stable | Generated DTO leakage into domain |
| Hexagonal | App → Domain | Command | No Mapping with domain-specific command/value objects | Full / Two-Way when complexity justifies it | Web/persistence DTO leakage into domain |
| Hexagonal | App → Domain | Query | No Mapping | Two-Way | External DTO leakage into domain |
| Hexagonal | Domain → Persistence | Any | Two-Way | — | Domain annotated as persistence entity |
| Three-layer | Web → App | Command | Two-Way | Full | No Mapping for complex writes |
| Three-layer | Web → App | Query | No Mapping | Two-Way | Generated DTO leakage into business logic |
| Three-layer | App → Domain | Any | No Mapping | Two-Way | External DTO leakage into domain model |
| Three-layer | Domain → Persistence | Any | Two-Way | No Mapping when intentionally using persistence model as domain model | Accidental coupling |
| VSA | Within slice | Any | No Mapping | Two-Way | Unclear boundary ownership |
| VSA | Between slices | Any | Full Mapping | Two-Way | Shared mutable models across slices |

### Mixed strategy assessment

If the service uses different strategies per boundary or per operation:

- Document each combination explicitly.
- Assess each independently against the advisory matrix.
- Note whether the mix appears:
  - **Intentional**: consistent, localized, and easy to explain.
  - **Accidental**: inconsistent, ad-hoc, or boundary ownership is unclear.
  - **Unverified**: insufficient evidence.

Intentional mix = acceptable design pattern.
Accidental mix = advisory finding worth reporting.

---

## Phase 3 — Mapper Code Smells (MANDATORY)

Regardless of which strategy is detected, ALWAYS inspect mapper classes for these smells.

These are the most critical part of this skill because they may indicate misplaced responsibility.

### Smell 1 — Business logic in mapper (HIGH risk)

#### Required output for Smell 1

If business logic in mapper is detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Business Logic in Mapper

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Mapper making a business decision
public OfferDto toDto(Offer offer) {
    if (offer.getPrice().compareTo(BigDecimal.ZERO) == 0) {
        return OfferDto.free();  // business rule: free offer handling
    }
    return new OfferDto(offer.getId(), offer.getPrice());
}

// Mapper calculating derived values
public OfferDto toDto(Offer offer) {
    BigDecimal finalPrice = offer.getBasePrice()
        .multiply(offer.getDiscount())  // business calculation
        .setScale(2, RoundingMode.HALF_UP);
    return new OfferDto(offer.getId(), finalPrice);
}
```

Detection:

- `if`/`switch` statements inside mapper methods that are not simple null checks or type conversions
- arithmetic calculations tied to business concepts
- price/tax/discount/status decisions inside mapper
- defaulting business values based on rules

Classification:

- Risk = HIGH
- Score Impact = NO by default
- Formalization Status = Candidate for MCP validation when mapper responsibility is covered by a read MCP standard

### Smell 2 — Mapper calling services or repositories (HIGH risk)

#### Required output for Smell 2

If mapper service/repository/client calls are detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Mapper Calling Services or Repositories

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Mapper with service dependency
@Component
public class OfferMapper {
    private final PriceService priceService;

    public OfferDto toDto(Offer offer) {
        Price enrichedPrice = priceService.enrich(offer.getPrice());
        return new OfferDto(offer.getId(), enrichedPrice);
    }
}

// Mapper calling repository
public OfferDto toDto(Offer offer) {
    Currency currency = currencyRepository.findById(offer.getCurrencyCode());
    return new OfferDto(offer.getId(), offer.getPrice(), currency.getSymbol());
}
```

Detection:

- `@Autowired`, constructor injection, or `@Inject` in mapper classes
- MapStruct `uses = {ServiceClass.class}` with non-mapper dependency
- mapper calling repository, client, service, provider, gateway, producer, publisher, or adapter
- mapper method with side-effect collaborator call

Classification:

- Risk = HIGH
- Score Impact = NO by default
- Formalization Status = Candidate for MCP validation when mapper responsibility is covered by a read MCP standard

### Smell 3 — Mapper performing validation (MEDIUM risk)

#### Required output for Smell 3

If mapper validation is detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Mapper Performing Validation

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Mapper validating business rules
public Offer toDomain(OfferDto dto) {
    if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
        throw new InvalidOfferException("Price must be positive");
    }
    return new Offer(dto.getId(), dto.getPrice());
}
```

Detection:

- `throw` statements inside mapper methods
- explicit validation logic inside mapper methods
- Bean Validation interpretation inside mapper
- domain rule enforcement inside mapper

Do NOT flag:

- simple null guard required by generated mapper
- defensive conversion fallback with no business meaning
- `Objects.requireNonNull` for mapper constructor dependencies

Classification:

- Risk = MEDIUM
- Score Impact = NO by default
- Formalization Status = Evaluator-proposed / Candidate for MCP validation when validation placement is covered by a read MCP standard

### Smell 4 — Domain model contaminated with framework annotations (HIGH risk)

#### Required output for Smell 4

If domain model framework annotation contamination is detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Domain Framework Annotation Contamination

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Domain entity with JPA/JSON annotations
@Entity
@Table(name = "offers")
public class Offer {
    @Id
    private Long id;

    @JsonProperty("offer_price")
    private BigDecimal price;
}
```

Detection:

- `@Entity`, `@Table`, `@Column`, `@Id`, `@Version` on classes in `domain/`
- `@JsonProperty`, `@JsonIgnore`, `@JsonInclude`, `@XmlElement` on classes in `domain/`
- OpenAPI/Swagger annotations on domain classes
- framework imports in domain model packages

Classification:

- Risk = HIGH
- Score Impact = NO by default
- Formalization Status = Candidate for MCP validation when the applicable architecture standard was read through MCP

### Smell 5 — Mapper with side effects (HIGH risk)

#### Required output for Smell 5

If mapper side effects are detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Mapper Side Effects

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Mapper that persists, sends events, or logs business events
public OfferDto toDto(Offer offer) {
    auditLog.record("offer_accessed", offer.getId());
    return new OfferDto(offer.getId(), offer.getPrice());
}
```

Detection:

- method calls that persist, publish, send, notify, audit, cache, mutate, or call external providers
- collaborator calls with side effects inside mapper methods
- mapper writes to static/global state

Classification:

- Risk = HIGH
- Score Impact = NO by default
- Formalization Status = Candidate for MCP validation when mapper responsibility is covered by a read MCP standard

### Smell 6 — God mapper / bloated mapper (MEDIUM risk)

#### Required output for Smell 6

If god mapper / bloated mapper candidates are detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — God Mapper / Bloated Mapper

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Single mapper handling many unrelated mapping responsibilities
public class OfferMapper {
    public OfferDto toDto(Offer offer) { ... }
    public Offer toDomain(OfferDto dto) { ... }
    public OfferSummaryDto toSummary(Offer offer) { ... }
    public OfferListItemDto toListItem(Offer offer) { ... }
    public OfferAuditDto toAudit(Offer offer) { ... }
    public OfferExportDto toExport(Offer offer) { ... }
}
```

Detection:

- mapper class with >8 public mapping methods
- mapper class with >300 lines
- mapper maps unrelated aggregate roots
- mapper imports multiple unrelated domain subdomains

Classification:

- Risk = MEDIUM
- Score Impact = NO
- Formalization Status = Evaluator-proposed

### Smell 7 — Semantic leakage (MEDIUM risk)

#### Required output for Smell 7

If semantic leakage is detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Semantic Leakage

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



```java
// Same DTO used for create, update, and read — fields are nullable for different operations
public class OfferDto {
    private String id;        // null on create, present on update/read
    private BigDecimal price; // always present
    private String status;    // only on read
    private String reason;    // only on cancellation
}
```

Detection:

- DTO used across multiple controller methods with different semantics
- same request model used for create + update + patch + read
- many nullable fields that only apply to specific operations
- mapper has conditional mapping based on operation type

Classification:

- Risk = MEDIUM
- Score Impact = NO
- Formalization Status = Evaluator-proposed / Pending Playbook formalization

### Smell 8 — Mapping duplication (LOW risk)

#### Required output for Smell 8

If mapping duplication is detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Mapping Duplication

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detection:

- same field mappings repeated in multiple mapper classes without shared base or utility
- repeated manual mapping blocks across adapters
- same conversion logic copied across web and provider mappers

Classification:

- Risk = LOW
- Score Impact = NO
- Formalization Status = Advisory

---

### Smell 9 — Mixed responsibility model (success/error conflation) (MEDIUM risk)

A domain/DTO type that conflates two distinct responsibilities — representing genuine business data, and transporting validation errors/warnings — forcing every consumer of that type (factories, registries, downstream services) to handle the "error" case as if it were a real instance of the domain concept it impersonates.

#### Required output for Smell 9

If a mixed responsibility model is detected, list every non-compliant class found in the inspected scope and explain why it does not comply.

Required table:

```markdown
#### Compliance List — Mixed Responsibility Model

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

Detection:

- a class/record declares both an errors/warnings-like field (e.g. `errors`, `warnings`, typed as `List<ErrorMessage>` or similar) and genuine business-data fields belonging to the domain concept it represents
- a family of "error" implementations of a domain interface/type exists purely to satisfy factories/registries/buses designed for real instances of that type (e.g. an implementation whose corresponding downstream factory always returns an empty result because the "instance" is not real domain data)
- adding a new validation error requires touching the same registries/factories used for adding a new real domain variant

#### Example

```java
// Non-compliant — AncillaryAvailability conflates real data with validation errors
class AncillaryAvailability {
    List<ItemPrice> itemPrices;   // real business data
    List<ErrorMessage> errors;    // validation errors — different responsibility
}

// An "error" implementation forced through the same pipeline as real ancillaries
class InvalidTaxonomyAvailability extends AncillaryAvailability { ... }
// ...and a factory that exists only because the registry demands one:
class InvalidTaxonomyServiceDefinitionFactory implements ServiceDefinitionFactory {
    List<ServiceDefinition> create(InvalidTaxonomyAvailability a) {
        return List.of(); // never produces a real ServiceDefinition
    }
}
```

```java
// Recommended — separate the error/warning channel from the data channel
record AncillariesResult(List<AncillaryAvailability> data, List<ErrorMessage> errors) {}
```

Classification:

- Type = Evaluator-proposed
- Risk = MEDIUM
- Score Impact = NO for general absence
- Score Impact = Candidate when a factory/registry entry exists solely to satisfy the pipeline for an error/warning case (e.g. a factory that always returns an empty result for that type)
- Formalization Status = Pending Playbook formalization

---

## Reporting Discipline

- Report ALL HIGH risk smells regardless of count.
- Report max 2 detailed MEDIUM risk findings per category unless pervasive.
- Group LOW risk findings into a single row when needed, but still list non-compliant classes in the compliance list.
- For strategy assessment: report ALL boundaries — do not suppress even if reasonable.
- For every executed rule/check, list the classes that do not comply and explain why.
- If mapper was not inspected → mark as `Unverified`.
- Do NOT report mapper smells from examples or generated comments; inspect actual production code.
- Do NOT report mapping volume as a smell by itself.
- Do NOT report absence of Full Mapping as a smell by itself.
- State whether findings are based on source inspection, grep/static pattern, or inferred structure.
- Aggregate counts or heuristic counts alone are not enough.
- If a heuristic produces candidates, inspect candidate classes before listing them as non-compliant.
- If candidate classes are not fully inspected, list them as `Candidate / Unverified`, not as non-compliant.

---

## Design Quality Deduplication

If a mapping or mapper responsibility finding was already reported in `Design Quality Findings`:

- Do NOT duplicate it as a separate finding.
- Reference the existing finding:

```text
See Design Quality → MCP-backed findings: [finding description]
```

Only add this skill's finding if it provides actionable boundary-level detail not covered by the original finding.

If this skill detects a potentially MCP-backed issue:

- Keep it under `Additional Quality Insights` by default.
- Mark it as `MCP Candidate` or `Candidate for MCP validation`.
- Let the main prompt decide whether it belongs in `Design Quality Findings`.

---

## Hotspot Consolidation (MANDATORY)

After executing all Phase 3 rules, scan for classes that appear in two or more rules within the same skill run.

If any class appears in ≥ 2 rules:

- Do NOT list it separately in each rule's output table as the primary finding.
- Summarize it as a single advisory hotspot entry with all affected rule IDs.
- Within the hotspot entry, state each rule's evidence, confidence, and risk separately.
- Place the consolidated hotspot in `## Additional Quality Insights` under an **Advisory Hotspots** subsection.

Example: if `ErrorEventAssembler` appears in `mapper_calling_services_or_repositories` (HIGH risk), `mapper_performing_validation` (MEDIUM risk), and `business_logic_in_mapper` (HIGH risk), produce one hotspot entry — not three separate table rows.

Required hotspot entry format:

```markdown
#### Advisory Hotspot — <ClassName>

File: `<relative path>`

| Rule | Confidence | Risk | Evidence |
|---|---|---|---|
| mapper_calling_services_or_repositories | HIGH | HIGH | <evidence from JSON> |
| mapper_performing_validation | MEDIUM | MEDIUM | <evidence from JSON> |
| business_logic_in_mapper | MEDIUM | HIGH | <evidence from JSON> |

Recommendation: <consolidated recommendation covering all three rules>

Score Impact = NO. Advisory only.
**Evidence:** `mapping-boundaries--<EXECUTION_DATE>.json` → `rules.<rule_id>.non_compliant_details`
```

Hotspots remain advisory. Consolidation does not imply additional scoring impact beyond what individual rules carry.

---

## Cross-Slice Systemic Pattern Detection (MANDATORY when expected_architecture is vertical-slice)

After executing Phase 3, if `expected_architecture` is `vertical-slice`, check whether `business_logic_in_mapper` findings span multiple distinct feature slices.

If `business_logic_in_mapper` is detected in mapper classes belonging to 4 or more distinct feature slices:

- Report it as a **systemic cross-slice pattern** in `## Additional Quality Insights`, not only as a count of individual classes.
- State which slices are affected by name.
- Note that a pattern spanning most or all slices may indicate a team-level convention rather than isolated technical debt. Use neutral wording — do not assert causation.
- List the affected classes as supporting evidence under the pattern, not as the primary finding.

If the pattern spans fewer than 4 slices, report it as individual findings per the standard Phase 3 output.

Required systemic pattern format:

```markdown
#### Advisory — Systemic Pattern: Business Logic in Mappers (Cross-Slice)

The `business_logic_in_mapper` finding was reported in mapper classes across <N> feature slices:
<slice-1>, <slice-2>, <slice-3>, <slice-N>.

This suggests the pattern may be a team-level convention rather than isolated technical debt.
Manual inspection is recommended to determine whether the business logic is intentional
enrichment or misplaced responsibility.

Affected classes (<N> total):
- `<class-1>` (`<slice>`)
- `<class-2>` (`<slice>`)
  ...

Score Impact = NO. Advisory only.
**Evidence:** `mapping-boundaries--<EXECUTION_DATE>.json` → `rules.business_logic_in_mapper.non_compliant_classes`
```

This detection is advisory. A systemic pattern does not carry different scoring weight than isolated findings until promoted by the main evaluation prompt with MCP/Playbook evidence.

---

## Playbook Integration Notes

This skill is designed to run within the Iberia Engineering Copilot evaluation platform.

### Placement in the evaluation workflow

This skill executes AFTER:

1. MCP discovery (`searchStandards` + `readStandard`)
2. Architecture validation
3. Design Quality analysis
4. Production code inspection

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
| `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` | Potential validation for domain purity, adapters, dependency direction |
| `docs/07-architecture-and-patterns/40-three-layer-architecture.md` | Potential validation for layer responsibilities |
| `docs/07-architecture-and-patterns/60-vertical-slice-architecture.md` | Potential validation for slice boundaries |
| `docs/02-foundations/fundamentals-checklist.md` | Potential validation for separation of concerns |
| `docs/03-tech-stack/java-style-guide.md` | General Java style; not a mapping strategy standard |

### execution-config.yml activation

To activate this skill, add to `execution-config.yml`:

```yaml
skills:
  - mapping-between-boundaries
```

Mapped file: `docs/skills/mapping-between-boundaries.md`

---

## Hard Rules

- Findings are Evaluator-proposed by default.
- Do NOT affect scoring directly.
- Do NOT classify findings as `MCP-backed`.
- Do NOT present findings as Playbook violations unless the exact MCP standard was read and listed by the main prompt.
- Do NOT declare architecture compliance or non-compliance.
- Do NOT prescribe a single correct strategy.
- Always reason in terms of trade-offs.
- Do NOT penalize based on mapping volume alone.
- Do NOT recommend Full Mapping everywhere.
- Full Mapping can be over-engineering for simple CRUD or read-only flows.
- Findings MUST include concrete class/method evidence.
- For every skill rule executed, the evaluator MUST list non-compliant classes and explain why each class does not comply.
- Aggregate counts or heuristic counts are not enough without class-level non-compliance details.
- Candidate classes found by grep/static pattern MUST be inspected before being classified as non-compliant.
- If a candidate was not inspected, mark it as `Candidate / Unverified`.
- HIGH risk mapper smells MUST include concrete class/method evidence.
- If code was not inspected → mark as `Unverified`.
- Risk level does not imply scoring impact.
- HIGH risk findings remain non-scoring unless the main prompt promotes them using MCP/Playbook evidence or authorized fallback.
- When a mapping strategy standard is published in the Playbook → update classification and mark covered findings as MCP-backed where applicable.

---

## Appendix — Quick Reference

### When each strategy makes sense

**No Mapping**

- Simple CRUD with no domain complexity
- Query-only operations where domain shape matches response needs
- Early stage / MVP where speed matters
- Within a vertical slice where the model is intentionally local to the slice
- Risk: acceptable only if domain stays clean of framework/infrastructure annotations

**Two-Way Mapping**

- Most common balanced approach
- When domain needs to evolve independently of API contract
- When persistence technology may change
- When generated OpenAPI models should remain outside application/domain
- Risk: boilerplate, but manageable with MapStruct or focused manual mapping

**Full Mapping**

- Complex write operations with multiple variants
- When different use cases need different input validation
- When API contract evolves faster than domain
- When command/query models improve clarity
- Risk: over-engineering for simple operations

**One-Way Mapping**

- When you want domain to be passable to outer layers without copy
- When read models are abundant and lightweight
- Risk: conceptually complex, harder to maintain

### MapStruct smell signals

If the service uses MapStruct:

- `@Mapper` with `uses = {ServiceClass.class}` → Smell 2 risk when class is not a mapper/converter
- `@BeforeMapping` / `@AfterMapping` with business logic → Smell 1 risk
- `@Condition` methods with business rules → Smell 1 risk
- `@Mapping(expression = "java(...)")` with complex expressions → Smell 1 risk
- `componentModel = "spring"` with injected non-mapper dependencies → Smell 2 risk
- abstract mapper with injected services/repositories → Smell 2 risk