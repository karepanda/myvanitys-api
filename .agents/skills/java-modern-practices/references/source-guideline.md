---
name: java-modern-practices
description: Identify maintainability, readability, type-safety, and runtime-risk findings related to modern Java usage (Java 21 LTS). Evaluates records, sealed classes, pattern matching, virtual threads, sequenced collections, immutability, streams, optional usage, exhaustive type registries without compile-time safety, in-memory caching anti-patterns, and JPA anti-patterns. NON-SCORING by default — findings may only affect evaluation score if the main evaluation prompt promotes them using concrete code evidence, MCP traceability when applicable, and explicit sub-criterion mapping.
license: Apache-2.0
metadata:
  author: iberia-engineering
  version: "2.4"
compatibility: Java 21 LTS. All findings must indicate minimum Java version required.
---

# Skill — Java Modern Practices (v2.4 — Java 21 LTS)

## Status

Evaluator extension. NON-SCORING by default.

This skill detects Java modernization opportunities and risks.

A finding from this skill MUST NOT affect score directly.

A finding MAY be promoted by the main evaluation prompt only when all conditions are met:

1. Concrete class/method/file evidence is inspected.
2. The finding demonstrates maintainability, readability, type-safety, correctness, or runtime-risk impact.
3. The finding maps to a framework sub-criterion, usually:
  - Code Structure → Java 21 Features
  - Code Structure → Code Quality
  - Code Quality → Code Standards
  - Code Quality → Tooling Integration
4. If the finding claims Playbook alignment or violation, the exact MCP standard was read and listed in `MCP Standards Used`.
5. The finding is moved to `Design Quality Findings` or the relevant scoring rationale by the main prompt.

General absence of a Java 21 feature MUST NOT be penalized by itself.

Concrete missed opportunities with repeated evidence MAY cap the relevant sub-criterion.

Runtime-risk findings MAY reduce the relevant sub-criterion when validated by the main prompt.

---

## Population Count Correction (2026-07-30)

**Prior bug (fixed):** All 13 `java_modern_practices` rules reported `candidate_classes_inspected` equal to `java_files_inspected` (total repository file count) instead of a structurally-scoped candidate population. This was caused by `buildRule(name, files.length, details)` passing the total file array length instead of the count of files matching the rule's structural detection gate. The non-compliant detection logic was correct — only the denominator was inflated, producing artificially lenient percentages and caps.

**Fixed population per rule:**

| Rule | Population (corrected) | Structural gate |
|---|---|---|
| `records` | Files containing `record` declarations | `/\brecord\s+\w+\s*\(/` |
| `optional` | Files with Optional as field/parameter/return or containing Optional.get() | `evidence.length > 0` |
| `streams` | Files with Stream API usage (Collectors.toList, multiple stream(), side-effectful streams) | `evidence.length > 0` |
| `immutability` | Files with mutable field patterns (non-final collections, exposed getters, unguarded record collections) | `evidence.length > 0` |
| `pattern_matching_switch` | Files with 3+ `instanceof` occurrences or old instanceof+cast patterns | `oldInstanceofCasts.length \|\| instanceofCount >= 3` |
| `sealed_classes` | Files with string-switch/equals-chain/enum-chain type discriminators (>=3 branches) | `evidence.length > 0` |
| `exhaustive_type_registry` | Files with `Map<Class<?>, ...>` registries | `Map<Class<?>, ...>` field presence |
| `functional_interfaces` | Files declaring single-method interfaces matching JDK functional shapes | `interfaceMatch` + `abstractMethods.length === 1` |
| `sequenced_collections` | Files with get(0)/get(size()-1)/Collections.reverse patterns | `occurrences.length > 0` |
| `text_blocks` | Files with multi-line string concatenation | `evidence.length > 0` |
| `jpa_antipatterns` | Files with JPA entity/repository usage exhibiting N+1 or in-memory-pagination risk | `evidence.length > 0` |
| `in_memory_cache_antipatterns` | Files with `private/protected/public Map<...> field = new HashMap/ConcurrentHashMap` | `hasMapField` gate |
| `virtual_threads` | All repository files (holistic config+file assessment, not per-file detection) | always inspects all files |

**Impact:** For rules where the structural gate is also the non-compliance gate (e.g., `optional`, `streams`, `immutability`, `pattern_matching_switch`, `sequenced_collections`), the percentage will now correctly reflect the ratio of non-compliant structural candidates to total candidates. For `records`, the population (record count) differs from the non-compliance count (records missing defensive copy), producing a non-100% rate.

**Note:** `virtual_threads` was intentionally NOT changed — its detection is a holistic config+file assessment, not per-file pattern detection. `files.length` is the correct population for this rule.

### Tautology Correction (2026-07-30, 2nd pass)

**Prior defect (fixed):** For 6 of the 12 rules, the candidate population gate was identical to the non-compliance gate, making `candidate_classes_inspected` mathematically equal to `non_compliant_classes_count` by construction. A rule built this way can never detect good adoption — any class that correctly adopted the modern practice was invisible to the population count entirely.

**Fixed candidate vs. violation gates:**

| Rule | Candidate gate (broader) | Violation gate (narrower subset) |
|---|---|---|
| `optional` | File references `Optional` at all | Uses Optional.get(), Optional as field, or Optional as parameter |
| `streams` | File uses `.stream()` calls | Collectors.toList(), >=4 stream() calls in same class, or side-effectful stream |
| `pattern_matching_switch` | File has 3+ `instanceof` checks of any kind (including pattern-matching) | Old-style `instanceof` + cast pattern (not pattern-matching instanceof) |
| `sealed_classes` | File has 3+ branch patterns (switch or if-else chains) | String-switch with 3+ cases, String.equals() chain with 3+ branches, or enum equality chain with 3+ branches |
| `sequenced_collections` | File references `List` or `ArrayList` | Manual `get(0)`, `get(size()-1)`, or `Collections.reverse()` |
| `immutability` | File has `private/protected/public List/Set/Map/Collection` field | Non-final mutable collection (domain), getter returns mutable collection directly, or record with collection component without defensive copy |
| `records` | (already correct, unchanged) Files with `record` declarations | Missing defensive copy on mutable fields |

**Impact (bps-j sample):**

| Rule | Before (C/N) | After (C/N) | Old% | New% |
|---|---|---|---|---|
| optional | 14/14 | 68/14 | 100% | 21% |
| streams | 6/6 | 44/6 | 100% | 14% |
| pattern_matching_switch | 4/4 | 4/0 | 100% | 0% |
| sealed_classes | 1/1 | 48/1 | 100% | 2% |
| sequenced_collections | 25/25 | 222/25 | 100% | 11% |
| immutability | 33/32 | 44/32 | 97% | 73% |

The `pattern_matching_switch` rule now correctly identifies 4 classes with 3+ `instanceof` checks — all using modern pattern-matching syntax (Java 16+), zero violations. Previously these 4 files were incorrectly flagged as needing modernization when they had already adopted it.

**Explicit note on the `pattern_matching_switch` 4→0 non-compliant change:** This is the ONLY rule among the 6 corrected in this session whose `non_compliant_classes_count` changed. Under the original tautological gate, the check `oldInstanceofCasts.length || instanceofCount >= 3` assumed every class with 3+ instanceof was non-compliant — the gate never actually distinguished between old-style `instanceof`+cast and modern pattern-matching `instanceof`. When the gate was separated (candidate: `instanceofCount >= 3`; violation: `oldInstanceofCasts.length` only), the 4 bps-j classes previously flagged were re-inspected under the real, narrower check and confirmed to already use pattern-matching instanceof correctly. This is a **correction of a false-positive finding**, not a change in detection scope. All other 5 rules (`optional`, `streams`, `sealed_classes`, `sequenced_collections`, `immutability`) preserved their exact non-compliant count — only candidate counts changed. Cross-check confirmed via full 7-service re-run.

---

## MCP Discovery Result

### Discovery date: 2026-04-30
### MCP Server: `engineering-standards`
### Sections searched: `07-architecture-and-patterns`, `05-testing-and-quality`, `03-tech-stack`, `02-foundations`

| Topic | MCP Coverage | Standard Path |
|-------|-------------|---------------|
| Java Records | NOT COVERED | — |
| Sealed classes | NOT COVERED | — |
| Pattern matching / switch | NOT COVERED | — |
| Virtual threads | NOT COVERED | — |
| Sequenced Collections | NOT COVERED | — |
| Text Blocks | NOT COVERED | — |
| Streams / Optional misuse | NOT COVERED | — |
| Immutability (value objects) | IMPLICIT | `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` |
| JPA N+1 / in-memory pagination | NOT COVERED | — |
| Coverage thresholds (80% new / 60% legacy) | EXPLICIT | `docs/05-testing-and-quality/jacoco.md` |
| Java style / naming conventions | EXPLICIT | `docs/03-tech-stack/java-style-guide.md` |

**Consequence:**

All findings in this skill are `Evaluator-proposed` unless the main evaluation prompt promotes them.

Default behavior:

- Evaluator-proposed findings MUST NOT affect scoring directly.
- Evaluator-proposed findings MUST NOT be presented as Playbook violations.
- Missing Java 21 feature adoption MUST NOT be penalized by presence/absence alone.

Promotion behavior:

A finding may become scoring-relevant only when the main prompt validates it with concrete code evidence and maps it to a scoring sub-criterion.

Examples:

- `No virtual threads detected` → non-scoring.
- `Fixed thread pool used for confirmed blocking I/O under high concurrency` → possible Code Structure / runtime-risk concern.
- `No sealed classes detected` → non-scoring.
- `Closed domain state hierarchy implemented with string branching across multiple classes` → possible maintainability/type-safety concern.
- `No pattern matching detected` → non-scoring.
- `Repeated instanceof + cast chains in inspected code` → possible readability/maintainability concern.

**Single MCP-aligned exception:**

§7 Immutability — when a mutable collection is exposed on a domain Value Object or Entity, the finding is a candidate for MCP-backed validation.

The Hexagonal Architecture standard (`docs/07-architecture-and-patterns/50-hexagonal-architecture.md`) prescribes immutable Value Objects with private constructors and final fields. A mutable collection exposed on a domain object may violate this prescription.

This finding MAY affect Code Quality scoring only if the main evaluation prompt:

1. reads the Hexagonal Architecture standard through MCP,
2. lists the exact standard path in `MCP Standards Used`,
3. validates the concrete class/method evidence,
4. promotes the finding under `Design Quality Findings`,
5. explicitly maps it to a scoring sub-criterion.

---

## Purpose

Identify maintainability, readability, type-safety, correctness, and runtime-risk findings related to modern Java usage (Java 21 LTS).

This skill complements — and does NOT override — MCP standard evaluation.

---

## Scope

Evaluate Java code for:

1. Records usage
2. Sealed classes / interfaces
3. Functional interfaces
4. Stream usage
5. Optional usage
6. Virtual threads (including pinning and ThreadLocal risks)
7. Immutability
8. Pattern matching / switch expressions (Java 21 stable)
9. Sequenced Collections (Java 21 stable)
10. Text Blocks
11. JPA Anti-patterns (N+1, in-memory pagination)

---

## Output Format

Add section **after** Design Quality Findings:

```markdown
## Additional Quality Insights

### Java Modern Practices Findings
```

Table:

| Finding | Class / Method | Evidence | Recommendation | Risk | Confidence | Java Version | Score Impact |
|---------|----------------|----------|----------------|------|------------|--------------|--------------|

Column definitions:

- **Risk**: `LOW` / `MEDIUM` / `HIGH`
  - LOW = readability / style opportunity
  - MEDIUM = potential runtime degradation, maintainability debt, or type-safety issue
  - HIGH = runtime risk, memory leak, correctness issue, or likely production degradation
- **Confidence**: `HIGH` / `MEDIUM` / `LOW`
  - HIGH = finding based on direct evidence in inspected code (AST, method body, annotation)
  - MEDIUM = inferred from partial evidence (signature, type, naming pattern)
  - LOW = speculative — code not fully inspected, or dependency chain assumed
- **Java Version**: minimum Java version required to apply the recommendation
- **Score Impact**:
  - `NO` = default for evaluator-proposed opportunities
  - `Candidate` = may be promoted by the main evaluation prompt
  - `See Design Quality` = already promoted or explicitly mapped by the main prompt

`Candidate` does not affect score by itself. It only signals that the finding has enough concrete evidence to be reviewed by the main evaluation prompt.

### Section B — Compliance Lists by Rule

For every rule/check executed by this skill, the evaluator MUST list the classes that do not comply and explain why they do not comply.

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

- Records
- Sealed classes / interfaces
- Functional interfaces
- Streams
- Optional
- Virtual Threads
- Immutability
- Pattern Matching / Switch Expressions
- Sequenced Collections
- Text Blocks
- JPA Anti-patterns

For each non-compliant class, `Why / Why not` MUST explain the specific best-practice condition that is not met.

Aggregate counts are allowed only as a summary.
They are not enough without class-level non-compliance details.

If a check is repository-wide, sampling is invalid.
If a check is sampled or targeted, the evaluator MUST state the inspected scope, sample size, selection basis, and confidence.

---

## Class-Level Compliance Reporting (MANDATORY)

For every rule executed by this skill, the evaluator MUST list the classes that do not comply and explain why each class does not comply.

The evaluator MUST NOT only provide general statements such as:

```text
modern Java opportunities detected
Optional misuse observed
immutability risks found
streams are complex in some classes
```

Those statements are incomplete unless followed by a class-level compliance list.

For each executed rule, include:

- inspection scope,
- candidate classes inspected,
- non-compliant class count,
- class names,
- reason why each class does not comply,
- concrete evidence,
- recommendation,
- minimum Java version required for the recommendation.

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

Candidate classes found by grep/static pattern MUST be inspected before being classified as non-compliant.

If a candidate was not fully inspected, list it as `Candidate / Unverified`, not as non-compliant.

For HIGH-risk findings, the inspection SHOULD be repository-wide for the relevant pattern unless the main prompt explicitly limits scope.

---

## Reporting Discipline (MANDATORY)

Signal over noise. Apply before finalizing output:

- Do NOT report more than **2 detailed finding rows per category** unless Risk = HIGH
- Prioritize: `HIGH` → `MEDIUM` → `LOW`
- HIGH-risk findings MUST NOT be suppressed
- For every executed rule/check, list the classes that do not comply and explain why
- Do NOT repeat the same pattern across multiple classes as separate finding rows — report once with the most representative evidence, but include the complete non-compliant class list:
  ```text
  Same pattern detected in 3 additional classes: OrderService, PriceService, CartService
  ```
- Group similar LOW-risk findings into a single row when they share the same recommendation, but still list non-compliant classes in the compliance list
- If total findings exceed **10 rows**, suppress LOW-risk finding rows and note:
  ```text
  [N] additional LOW-risk opportunities suppressed. Run targeted review for full list.
  ```
- Do NOT suppress HIGH or mandatory evidence findings because of row limits
- Aggregate counts or heuristic counts alone are not enough
- If a heuristic produces candidates, inspect candidate classes before listing them as non-compliant
- If candidate classes are not fully inspected, list them as `Candidate / Unverified`, not as non-compliant

---

## Design Quality Deduplication (MANDATORY)

If a finding in this skill overlaps with a finding already reported in the Design Quality section:

- Do NOT duplicate
- Reference the existing finding:
  ```text
  See Design Quality → MCP-backed findings: [finding description]
  ```
- Only add a Java-specific supplement if it provides actionable detail not covered by the original finding

---

## Promotion Boundary (MANDATORY)

This skill detects candidates. The main evaluation prompt validates and promotes.

This skill MUST NOT:

- change scores directly
- classify a finding as `MCP-backed`
- claim Playbook violation unless the main prompt has read and listed the exact MCP standard
- reduce a dimension score
- reduce a sub-criterion score
- promote a finding to Design Quality by itself

This skill MAY classify a finding as:

- `Evaluator-proposed`
- `Candidate`
- `Observed`
- `Heuristic`
- `Additional Quality Insight`

The main prompt MAY promote a `Candidate` to scoring impact only if it validates:

1. concrete code/report evidence,
2. affected class/method/file,
3. relevant framework sub-criterion,
4. MCP traceability when Playbook alignment is claimed,
5. no-double-penalty compliance.

---

## Rules

---

### 1. Records

#### Required output for Records

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- record candidates are pure data carriers but remain classes without documented reason; existing records expose mutable components; record is used where framework proxying/no-args constructor is required; record misses invariant validation in compact constructor

Required table:

```markdown
#### Compliance List — Records

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Evaluate both candidate classes and existing records.

#### Candidate detection

Detect classes that are pure data carriers:

- only fields + constructor + getters + equals / hashCode / toString
- no behavior
- no framework proxying requirement
- no lifecycle behavior
- no identity semantics beyond field equality

**Detect REST response DTOs as record candidates — with contract stability check.**

```java
// Candidate — internal / stable DTO
public class OfferResponse {
    private final String id;
    private final BigDecimal price;
    // constructor, getters, equals, hashCode, toString
}

// Recommended — Java 21 + Spring Boot 3.x / Jackson 2.15+
public record OfferResponse(String id, BigDecimal price) {}
```

**Do NOT recommend record when:**

- DTO is part of a public or versioned API contract (OpenAPI-published, used by external consumers)
- fields are frequently added or removed (evolving contract)
- backward JSON compatibility is required (`@JsonIgnoreProperties` patterns signal instability)
- DTO is request body with optional fields relying on setter-based binding
- class is a domain entity or value object where a class is justified by:
  - private constructors
  - static factories
  - invariant validation
  - identity
  - lifecycle rules
  - behavior-rich domain modeling
  - controlled creation with `of(...)`
  - encapsulated mutation rules

In domain code, prefer domain clarity over language novelty.

#### Existing record validation

Check:

- record represents immutable data
- record does not expose mutable collections without defensive copy
- compact constructor validates invariants when needed
- record is not used as a JPA entity
- record is not used where framework proxying or no-args constructor is required
- record does not contain excessive business logic
- record components use domain-relevant names
- nullable components are intentional and handled

```java
// Correct — defensive copy in compact constructor
public record Offer(List<OfferItem> items) {
    public Offer {
        items = List.copyOf(items);
    }
}

// Risk: MEDIUM — callers can mutate items
public record Offer(List<OfferItem> items) {}
```

Classification:

- Type = Evaluator-proposed
- Risk = LOW for isolated candidate
- Risk = MEDIUM for repeated boilerplate across DTOs, commands, queries, immutable projections, or simple value carriers
- Score Impact = NO for isolated cases
- Score Impact = Candidate for repeated maintainability debt with concrete examples
- Java Version = 16+

---

### 2. Sealed classes / interfaces

#### Required output for Sealed Classes / Interfaces

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- closed hierarchy is implemented with strings/enums/repeated branching; finite implementations are known but type-safety is not enforced; recommendation is not valid if dynamic extension/proxying is expected

Required table:

```markdown
#### Compliance List — Sealed Classes / Interfaces

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect closed polymorphic hierarchies.

Good candidates:

- known finite set of implementations
- domain alternatives
- result models
- event models
- provider variants known at compile time
- state models where all states are known

Do NOT recommend sealed types if:

- Spring needs dynamic proxies
- external extension is expected
- plugin architecture is used
- implementations are discovered dynamically
- additional variants are expected from external teams or external providers

Classification:

- Type = Evaluator-proposed
- Risk = LOW for isolated opportunity
- Risk = MEDIUM if string branching, weak enums, or repeated type checks reduce type-safety across business logic
- Score Impact = NO for general absence
- Score Impact = Candidate when a closed hierarchy is implemented through strings, enums with behavior leakage, or repeated branching that reduces type-safety
- Java Version = 17+ (stable)

---

### 3. Functional interfaces

#### Required output for Functional Interfaces

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- single-method interface duplicates standard Java functional interface without domain meaning; interface is not a port/use case/domain capability

Required table:

```markdown
#### Compliance List — Functional Interfaces

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect single-method interfaces used only as behavior parameters that duplicate standard Java functional interfaces.

Possible alternatives: `Function`, `Predicate`, `Consumer`, `Supplier`, `BiFunction`

Do NOT recommend replacement if:

- the interface has domain meaning
- the name improves ubiquitous language
- the interface may evolve
- the interface expresses a port, use case, or domain capability
- the interface belongs to the architecture boundary

Classification:

- Type = Evaluator-proposed
- Risk = LOW
- Score Impact = NO
- Java Version = 8+

---

### 4. Streams

#### Required output for Streams

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- stream chain is deeply nested; stream has side effects; repeated traversal creates performance/readability risk; outdated `collect(Collectors.toList())` is used where unmodifiable `toList()` intent is safe

Required table:

```markdown
#### Compliance List — Streams

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect risky stream usage:

- deeply nested streams
- streams with side effects
- hard-to-read chains
- repeated traversal of the same collection
- stream chains hiding business rules
- streams performing multiple responsibilities in a single expression

**Detect outdated collection idioms:**

```java
// Outdated — Java 8
stream.collect(Collectors.toList())

// Preferred — Java 16+
stream.toList()
```

Note: `stream.toList()` returns an unmodifiable list. If mutability is required, `Collectors.toList()` is still appropriate — verify intent before reporting.

Classification:

- Type = Evaluator-proposed
- Risk = LOW for style-only replacements like `stream.toList()`
- Risk = MEDIUM when deeply nested streams hide business rules or create repeated traversal/performance risk
- Score Impact = NO for style-only replacements
- Score Impact = Candidate when concrete inspected code shows maintainability or performance risk
- Java Version = 16+ (for `toList()`)

---

### 5. Optional

#### Required output for Optional

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- `Optional` is used as field/parameter; `Optional.get()` is used without presence check; null is returned where Optional would clarify absence

Required table:

```markdown
#### Compliance List — Optional

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect:

- `Optional` used as field → Risk = MEDIUM
- `Optional` used as method parameter → Risk = MEDIUM
- `null` returned where `Optional` clarifies absence → Risk = LOW
- `Optional.get()` without presence check → Risk = HIGH

```java
// Risk: HIGH — exception if not present
Optional<Order> order = findOrder(id);
return order.get().total();

// Correct
return order.orElseThrow(() -> new OrderNotFoundException(id));
```

MCP note: `docs/03-tech-stack/java-style-guide.md` may prescribe `Optional` for return values that may be absent. A Playbook-aligned claim requires the main prompt to read that standard through MCP and list it in `MCP Standards Used`.

Classification:

- `Optional.get()` without check → Score Impact = Candidate
- `Optional` as field/parameter → Score Impact = NO unless repeated and causing API confusion
- `null` returned where `Optional` clarifies absence → Score Impact = NO unless repeated across public application APIs
- Java Version = 8+

---

### 6. Virtual Threads

#### Required output for Virtual Threads

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- fixed thread pool with confirmed blocking I/O; virtual thread pinning risk; ThreadLocal leak risk when virtual threads are enabled/proposed

Required table:

```markdown
#### Compliance List — Virtual Threads

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



**Precondition gate — MANDATORY before any VT analysis:**

Virtual thread findings MUST NOT be reported unless ALL of the following are confirmed:

- Executor usage is explicitly detected (`ExecutorService`, `@Async`, thread pool config in `application.yml`)  
  OR `spring.threads.virtual.enabled=true` is present
- Service performs confirmed blocking IO (JDBC, blocking HTTP client, file IO)

Do NOT speculate about the concurrency model.

If executor is not visible → mark as `Unverified` and skip.

General absence of virtual threads MUST NOT be reported as a finding.

---

#### 6.1 Opportunity — Risk: LOW / MEDIUM

```java
// Candidate: fixed pool + blocking IO
Executors.newFixedThreadPool(200)
```

Recommendation: consider `Executors.newVirtualThreadPerTaskExecutor()` (Java 21) when the workload is blocking I/O bound and the concurrency model justifies it.

- Score Impact = NO for isolated opportunity
- Score Impact = Candidate only when fixed thread pools or blocking concurrency patterns are directly inspected and create scalability/runtime-risk concerns
- Java Version = 21+

#### 6.2 Pinning — Risk: HIGH

Blocking IO inside `synchronized` pins the virtual thread to its carrier, negating VT benefits.

```java
// HIGH risk
synchronized (lock) {
    result = jdbcTemplate.query(...);
}
```

Recommendation: replace `synchronized` with `ReentrantLock` when wrapping IO.

- Score Impact = Candidate when VT is enabled or explicitly proposed in code/config
- Java Version = 21+

#### 6.3 ThreadLocal leak — Risk: MEDIUM

`ThreadLocal` state is not automatically cleared with virtual threads, risking memory leaks at scale.

```java
// MEDIUM risk
private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();
```

Recommendation: consider `ScopedValue` (Java 21 preview) or explicit `finally` cleanup.

- Score Impact = Candidate when VT is enabled or explicitly proposed in code/config
- Java Version = 21+

---

### 7. Immutability

#### Required output for Immutability

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- mutable collection exposed; missing defensive copy; non-final fields in value-like object; mutable object crosses boundary; record exposes mutable component

Required table:

```markdown
#### Compliance List — Immutability

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect mutable DTOs / models where immutability would improve safety.

Report:

- mutable collections exposed on domain objects (Value Objects, Entities)
- setters in model objects
- defensive copy missing
- non-final fields in value-like objects
- records with mutable components and no defensive copy
- mutable objects crossing architectural boundaries

```java
// Risk: MEDIUM — mutable list exposed on domain object
public class PriceTable {
    private List<Price> prices;
    public List<Price> getPrices() { return prices; }
}

// Recommended
public List<Price> getPrices() { return Collections.unmodifiableList(prices); }
```

**MCP alignment candidate:**

When a mutable collection is exposed on a domain Value Object or Entity, this is a candidate for MCP-backed validation.

The main prompt must validate against `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` before classifying it as MCP-backed.

Classification:

- Type = Evaluator-proposed by default
- Type = Candidate MCP-backed finding only after main-prompt validation
- Risk = MEDIUM
- Score Impact = Candidate for domain objects / NO for DTOs or application layer unless repeated and harmful
- Java Version = 8+

---

### 8. Pattern Matching / Switch Expressions

#### Required output for Pattern Matching / Switch Expressions

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- repeated instanceof + cast chains; 3+ branch finite hierarchy can be expressed with Java 21 switch patterns; guarded pattern could reduce nested conditionals

Required table:

```markdown
#### Compliance List — Pattern Matching / Switch Expressions

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



#### 8.1 instanceof chains (Java 16+) — Risk: LOW / MEDIUM

```java
// Candidate
if (shape instanceof Circle) {
    Circle c = (Circle) shape;
    ...
}

// Java 16+ pattern matching
if (shape instanceof Circle c) { ... }
```

#### 8.2 Switch pattern matching — Java 21 stable (JEP 441) — Risk: LOW / MEDIUM

Detect 3+ branch `instanceof` chains over finite type hierarchies.

```java
// Java 21
return switch (shape) {
    case Circle c    -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t  -> (t.base() * t.height()) / 2.0;
};
```

#### 8.3 Guarded patterns — Java 21 stable — Risk: LOW

```java
// Candidate — nested if inside instanceof
if (shape instanceof Circle c) {
    if (c.radius() > 0) { ... }
}

// Java 21
case Circle c when c.radius() > 0 -> ...
```

Classification:

- Type = Evaluator-proposed
- Risk = LOW for isolated instanceof chains
- Risk = MEDIUM when repeated instanceof + cast chains create readability, maintainability, or type-safety debt across business logic
- Score Impact = NO for isolated cases
- Score Impact = Candidate for repeated inspected patterns in business logic
- Java Version = 16+ / 21+

---

### 9. Sequenced Collections (Java 21 — JEP 431)

#### Required output for Sequenced Collections

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- manual first/last element access is repeated; manual reverse/copy logic creates boundary-condition risk

Required table:

```markdown
#### Compliance List — Sequenced Collections

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect manual first/last element access.

```java
// Candidates
list.get(0)
list.get(list.size() - 1)
new ArrayList<>(set).get(0)
Collections.reverse(new ArrayList<>(list)).stream()...

// Java 21
list.getFirst()
list.getLast()
list.reversed().stream()...
```

Classification:

- Type = Evaluator-proposed
- Risk = LOW
- Score Impact = NO for isolated style replacements
- Score Impact = Candidate only when manual first/last handling is repeated and creates boundary-condition risk
- Java Version = 21+

---

### 10. Text Blocks

#### Required output for Text Blocks

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- multiline SQL/JSON/XML/HTML/log strings are built via concatenation or String.format with readability/correctness risk

Required table:

```markdown
#### Compliance List — Text Blocks

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



Detect multiline string construction via concatenation or `String.format` for SQL, JSON, XML, HTML, or multiline log messages.

```java
// Candidate
String sql = "SELECT o.id " +
             "FROM orders o " +
             "WHERE o.status = 'PENDING'";

// Java 15+
String sql = """
        SELECT o.id
        FROM orders o
        WHERE o.status = 'PENDING'
        """;
```

Do NOT report for single-line strings or strings with no readability benefit.

Classification:

- Type = Evaluator-proposed
- Risk = LOW
- Score Impact = NO for isolated cases
- Score Impact = Candidate when repeated multiline string construction creates readability or correctness risk
- Java Version = 15+

---

### 11. JPA Anti-patterns

#### Required output for JPA Anti-patterns

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- N+1 select risk; join fetch with Pageable/in-memory pagination; OSIV risk when runtime evidence exists

Required table:

```markdown
#### Compliance List — JPA Anti-patterns

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```



**Precondition gate — MANDATORY before any JPA analysis:**

JPA analysis MUST only run if at least one of the following is confirmed:

- `spring-boot-starter-data-jpa` in `pom.xml` or `build.gradle`
- `hibernate-core` in dependency tree
- `@Entity`, `@Repository`, or `JpaRepository` detected in inspected code

If none confirmed → skip this section entirely.

---

#### 11.1 N+1 SELECT — Risk: HIGH

Detect lazy-loaded associations accessed inside loops or streams.

```java
// HIGH risk — N+1
List<Post> posts = postRepository.findAll();
posts.forEach(p -> p.getComments().size());

// Recommended
@Query("select p from Post p left join fetch p.comments")
List<Post> findAllWithComments();
```

Score Impact = Candidate with direct inspected evidence.

#### 11.2 In-memory pagination — Risk: HIGH

Detect `JOIN FETCH` + Spring Data `Pageable` — triggers HHH000104 warning.

```java
// HIGH risk
@Query("select p from Post p left join fetch p.comments")
Page<Post> findAllWithComments(Pageable pageable);
```

Recommended two-query pattern:

```java
// Step 1 — paginate IDs
@Query("select p.id from Post p order by p.id")
Page<Long> findPostIds(Pageable pageable);

// Step 2 — fetch with JOIN FETCH for those IDs
@Query("select distinct p from Post p left join fetch p.comments where p.id in :ids")
List<Post> findAllByIdInWithComments(@Param("ids") Collection<Long> ids);

// Service assembly
Page<Long> idsPage = postRepository.findPostIds(pageable);
if (idsPage.isEmpty()) return Page.empty(pageable);
List<Post> posts = postRepository.findAllByIdInWithComments(idsPage.getContent());
return new PageImpl<>(posts, pageable, idsPage.getTotalElements());
```

Score Impact = Candidate with direct inspected evidence.

#### 11.3 OSIV (Open Session in View) — Risk: MEDIUM

Detect absence of `spring.jpa.open-in-view=false`. OSIV keeps DB session open for full HTTP request lifecycle, masking N+1 in production.

Score Impact = NO unless runtime risk is directly evidenced.

All JPA findings:

- Type = Evaluator-proposed
- Score Impact = Candidate for N+1 / in-memory pagination with concrete inspected evidence
- Score Impact = NO for OSIV absence/unknown unless runtime risk is directly evidenced
- Java Version = 8+

---

### 13. In-Memory Caching Anti-patterns (architecture-agnostic)

**Population:** All production `.java` files containing a class-level `Map`/`HashMap`/`ConcurrentHashMap` field (declared with visibility modifier + `= new ...HashMap<>()` initialization) that is mutated via `.put()`/`.putAll()`/`.putIfAbsent()` in a caching pattern, EXCLUDING:
- Proper caching frameworks (Caffeine, `@Cacheable`, `removeEldestEntry`)
- `@ConfigurationProperties` Spring config maps (one-time framework binding)
- One-time-populated lookup tables where ALL `.put()` calls are confined to constructors, static initializers, or `load`/`init`/`setup` methods
- Request-scoped memos where `.clear()` is unconditional (not gated by a size check) — these are legitimate reset-at-start patterns, not caches

### Candidate Precision Correction (2026-07-30)

**Prior false-positive rate (corrected):** Manual review of 8 flagged classes across bps-j and crm-agencies found 6/8 (75%) were false positives — static lookup tables loaded once from properties/enums (`BagTagNormalizer`, `BagTagProcessor`, `AirportRegion`, `SfxErrorMapping`), Spring `@ConfigurationProperties` config maps (`SalesforceQueriesProperties`), and request-scoped memos with legitimate unconditional `.clear()` resets (`ForwardCheckableDataSource`). The scanner's original candidate gate (`hasMapField + hasPutOperation - hasProperCache`) was too broad.

**Key structural distinction — `.clear()` as reset vs. crude eviction:**

| Pattern | Example | Signal | Classification |
|---|---|---|---|
| Unconditional `.clear()` at start of lifecycle method | `ForwardCheckableDataSource.unload()`: `forwardsById.clear();` (line 38, no size check nearby) | `.clear()` NOT preceded by `size() > MAX` — reset-at-start | NOT a cache (legitimate memo reset) |
| Conditional `.clear()` inside size-cap check | `SateDedupCache.cleanUp()`: `if (entries.size() > MAX_ENTRIES) { entries.clear(); }` (line 110-111) | `.clear()` gated by `if (size() > MAX_ENTRIES)` — crude eviction | Cache with crude eviction (genuine anti-pattern) |

**Corrected detection heuristics:**
1. Classes where ALL `.put()`/`.putAll()` calls are in constructors or `load`/`init`/`setup`-named methods → excluded (one-time populated)
2. `@ConfigurationProperties`-annotated classes → excluded (Spring config binding)
3. `.clear()` only counts as violation when it appears within 300 chars of a size-check pattern (`size() > X` or `MAX_ENTRIES`/`MAX_SIZE`/`MAX_CAPACITY`/`MAX_LIMIT`) → otherwise it's a legitimate reset

**Benchmark — ForwardCheckableDataSource vs SateDedupCache from `scan_java_modern_practices.ts`**:
- `ForwardCheckableDataSource`: map field ✓, `.put()` ✓, `.clear()` ✓ but unconditional → excluded entirely. `isClearConditionallyGated` returns `false` (no `size() > MAX` within 300 chars before `.clear()`).
- `SateDedupCache`: map field ✓, `.put()` ✓, `.clear()` ✓ AND `entries.size() > MAX_ENTRIES` on line 110, within 300 chars of `.clear()` on line 111 → `isClearConditionallyGated` returns `true` → flagged.

**Findings (two categories):**

1. **Unbounded map used as cache** — field with no size-bound check (`size() > MAX_ENTRIES` or similar) and no proper eviction policy. Unbounded memory growth risk.
2. **Capped with full-clear eviction** — field with a size cap but evicts via `.clear()` on the entire map instead of removing individual entries selectively (e.g., LRU). Wipes all cached data on overflow instead of evicting the least-recently-used entry.

**Confidence:** MEDIUM. Detection is pattern-based (Map field + put operations + absence of proper framework). Classes creating maps as per-request local variables or one-time initializers are excluded via the `private|protected|public` + `fieldName =` pattern.

**Architecture:** This rule is architecture-agnostic — it applies equally to hexagonal, three-layer, and vertical-slice services. It is NOT gated behind hexagonal architecture.

**Real examples (bps-j):**

| Class | Finding | Detail |
|---|---|---|---|
| `AutoPirDedupStore` | Unbounded map | `private final Map<String, Instant> createdAt = new ConcurrentHashMap<>()` — no maximum-size bound, only time-based expiration via iterator. |
| `SateDedupCache` | Capped + full-clear | `private final Map<String, Entry> entries = new ConcurrentHashMap<>()` — has `MAX_ENTRIES = 500_000` but on exceed: `entries.clear()` wipes everything (line 110-111: `if (entries.size() > MAX_ENTRIES) { entries.clear(); }`). |

**Corrected cross-service findings (2026-07-30, post precision fix):**

| Service | Architecture | Inspected | Non-compliant | Detail |
|---|---|---|---|---|
| bps-j | hexagonal | 2 | 2 | AutoPirDedupStore, SateDedupCache |
| offer-manager | hexagonal | 0 | 0 | — |
| offer-availability | hexagonal | 9 | 9 | CabinType, QPXCabinType, DataList, 6 in-memory repo configs |
| crm-agencies | vertical-slice | 0 | 0 | Prior 2 findings (SfxErrorMapping, SalesforceQueriesProperties) were false positives excluded by precision fix |
| product-catalog-resource-access | three-layer | 0 | 0 | — |
| ndc-api-adapter | vertical-slice | 0 | 0 | — |

**Recommended fix:** Replace raw `ConcurrentHashMap`/`HashMap` cache fields with:
- `Caffeine.newBuilder().maximumSize(N).expireAfterWrite(T, unit).build()` — Java-native, bounded, time-evicted
- `LinkedHashMap` with `removeEldestEntry()` override — bounded, LRU eviction without external dependencies
- `@Cacheable` + Spring Cache abstraction with proper eviction policy — framework-managed

**Negative case:** `CacheConfig` (offer-manager) — uses `Caffeine.newBuilder().expireAfterWrite(60, SECONDS)` with `@EnableCaching` and `@Cacheable` annotations. Properly bounded and managed by the framework. NOT flagged.

---

### 14. Persistence Roundtrip Inefficiency (architecture-agnostic)

**Rule ID:** `JAVA_PERSISTENCE_ROUNDTRIP_INEFFICIENCY` — evidence key `rules.persistence_roundtrip_inefficiency`.
**Added:** Gap #10 (Wave 2b). Sibling of `JAVA_JPA_ANTIPATTERNS` — that rule covers read-side N+1 / in-memory pagination; this one covers write-side roundtrips. They are separate rules with separate populations so their evidence stays individually reasonable.

**Population (scored):** production `.java` classes that perform a persistence write (contain a `.save(` / `.saveAndFlush(` call). This is `candidate_classes_inspected`.

**Scored sub-pattern — Check-then-insert:** within a SINGLE method, an `existsBy*(...)` / `existsById(...)` existence check that precedes a `.save(` / `.saveAndFlush(`. Costs an extra roundtrip; a native upsert (`INSERT ... ON CONFLICT DO NOTHING/UPDATE`) or a DB unique constraint with a plain `save()` achieves the same idempotency in one roundtrip. Only this sub-pattern feeds `non_compliant_classes_count`.

**Method-scoping is mandatory (precision):** the check MUST be method-scoped. A persistence-port adapter that merely EXPOSES `existsByRawMessage()` and `save()` as two SEPARATE port methods (e.g. `BaggageMessagePersistenceAdapter`, `SsmMessagePersistenceAdapter`) is NOT a check-then-insert and MUST NOT be flagged — same false-positive class as the mapper/validator precision issues in earlier gaps.

**Two informational sub-patterns (detected, reported, NOT scored):**

1. **Full-entity resave** — a `findById()` / `findBy*()` lookup followed by a `save()` of the retrieved/rebuilt entity in the same method. Reported under `full_entity_resave_observations`. NOT scored, because immutable-record upserts (hexagonal domain records) legitimately rebuild the whole record even when only a subset of fields changed — static analysis cannot reliably distinguish "subset changed" from "all changed", so scoring it would produce false positives.
2. **Non-selective `@Modifying` UPDATE** — a `@Modifying @Query("UPDATE ... SET col1=..., col2=..., ...")` whose SET clause assigns two or more columns. Reported under `non_selective_update_observations`. NOT scored: there is currently ZERO real occurrence across the 7 in-scope services (the only `@Modifying` in the fleet is a `DELETE` cleanup), so there is no evidence base to calibrate a penalty.

> **Why the split (scored vs informational):** mirrors the Gap #12 convention — score only the sub-pattern that is both reliably detectable and grounded in real fleet evidence; keep low-confidence / no-evidence sub-patterns as informational observations until evidence accumulates. This is a revisitable decision, not a permanent one: if real non-selective UPDATEs or clearly-partial resaves appear in future scans, either can be promoted to scored.

**Real examples (bps-j):**

| Class | Sub-pattern | Detail |
|---|---|---|
| `CreateMessageService` | Check-then-insert (SCORED) | `create()`: `if (messageRepository.existsByRawMessage(message)) { return; } messageRepository.save(message);` — the `{type}_raw_message_unique` constraint already guards idempotency, so the pre-check is a redundant roundtrip. |
| `WtInboxPersistenceAdapter` | Check-then-insert (SCORED) | `insertMessage()`: `if (repository.existsByKeyAndTypeAndSavedAtIsNull(key, type.name())) { return; } ... repository.save(entity);` — origin used `ON CONFLICT ("key","type") DO NOTHING`; the Java port replaced it with check-then-insert. |
| `UpsertAppleAirtagReportUseCase` | Full-entity resave (informational) | `upsert()`: `findByCriteria(...)` then rebuilds the full `AppleAirtagReport` record (preserving id/createdAt, refreshing url + updatedAt) and `save()`s it — only 2 of 7 fields change, but the whole record is re-persisted. Informational because rebuilding an immutable record is idiomatic. |

**Negative / edge cases (NOT flagged as scored):**

| Class shape | Why not scored |
|---|---|
| Port adapter exposing `existsByRawMessage()` and `save()` as separate methods (`BaggageMessagePersistenceAdapter`, `SsmMessagePersistenceAdapter`) | No in-method existence-check-then-save; method-scoping excludes it. |
| Direct `save()` with no preceding existence check (relies on DB unique constraint) | The efficient pattern — a single write. |
| `findById()` + `save()` where ALL fields legitimately change | Informational observation only; never increments the scored count (no indiscriminate flagging). |
| Selective single-column `@Modifying @Query("UPDATE ... SET status=:s WHERE id=:id")` | Only one column assigned — not a non-selective update. |

**Cross-service findings (7 in-scope services):**

| Service | Architecture | Candidates (writes) | Scored check-then-insert | Resave (info) | Non-selective UPDATE (info) |
|---|---|---|---|---|---|
| bps-j | hexagonal | 14 | 2 (CreateMessageService, WtInboxPersistenceAdapter) | 3 | 0 |
| offer-availability | hexagonal | 2 | 0 | 0 | 0 |
| offer-manager | hexagonal | 3 | 0 | 0 | 0 |
| airline-profile | three-layer | 0 | 0 | 0 | 0 |
| crm-agencies | vertical-slice | 0 | 0 | 0 | 0 |
| ndc-api-adapter | vertical-slice | 0 | 0 | 0 | 0 |
| product-catalog-resource-access | three-layer | 0 | 0 | 0 | 0 |

**Stale-backlog note:** the original backlog attributed a full-entity-resave pattern to `WtInboxProcessorService`. Today that class flushes retry state via `WtInboxPersistenceAdapter.updateProcessedState()`, which uses `entityManager.getReference(...)` + setters (managed-entity dirty checking, no `.save()`) — an EFFICIENT pattern, correctly not flagged. `ReportUpsertService` uses `findIdForUpsert() → isPresent → update()/insert()` (a check-then-write via `find`, not `existsBy` + `save`), so it is likewise not matched by the scored heuristic.

**Confidence:** MEDIUM. Detection is method-scoped regex over stripped source.

**Architecture:** architecture-agnostic — `expected_architecture: ["any"]`. Joins the `java_21_features` `no_double_penalty_group`. Calibration: `base_cap: 9.0`, `penalty_per_percent: 0.04` (performance/scalability concern, not pure style).

**Recommended fix:** replace check-then-insert with a single native upsert (`INSERT ... ON CONFLICT DO NOTHING/UPDATE`) or a plain `save()` backed by a DB unique constraint that enforces idempotency; replace full-entity resaves with targeted `@Query` UPDATEs when only a subset of fields changed.

---

### 12. Exhaustive Type Registry Without Compile-Time Safety

Detect manual, per-type dispatch registries (`Map<Class<?>, X>` resolved via `computeIfAbsent`/`getOrDefault`/`get` + null-check) whose only safety net for an unregistered type is a runtime exception, instead of a compiler-enforced exhaustive `switch` over a `sealed` hierarchy.

This is the runtime-registry counterpart of §2 (Sealed Classes / Interfaces): §2 covers closed hierarchies expressed as string/enum branching; this rule covers the same underlying problem expressed as a type-keyed registry.

#### Required output for Exhaustive Type Registry

When this rule is executed, the evaluator MUST list every non-compliant class found in the inspected scope and explain why it does not comply.

Examples of `Why / Why not`:

- type-keyed registry (`Map<Class<?>, X>` or equivalent) whose fallback throws a `RuntimeException`/unchecked exception when a type is not registered, instead of failing at compile time

Required table:

```markdown
#### Compliance List — Exhaustive Type Registry

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Non-compliant classes: <N>

| Class | Complies? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

#### Example

```java
// Non-compliant — exhaustiveness enforced only at runtime
Map<Class<?>, AncillaryAvailabilityFactory> factories = ...;

AncillaryAvailabilityFactory factory = factories.computeIfAbsent(dto.getClass(),
    clazz -> { throw new RuntimeException("No factory defined for " + clazz); });
```

```java
// Recommended — exhaustiveness enforced at compile time
sealed interface AncillaryAvailabilityDto
    permits BaggageAvailabilityDto, PriorityBoardingAvailabilityDto, InvalidTaxonomyAvailabilityDto {}

AncillaryAvailability availability = switch (dto) {
    case BaggageAvailabilityDto b -> mapBaggage(b);
    case PriorityBoardingAvailabilityDto p -> mapPriorityBoarding(p);
    case InvalidTaxonomyAvailabilityDto i -> mapInvalidTaxonomy(i);
    // adding a new permitted type without a case here is a compile error
};
```

Do NOT recommend this refactor if:

- the set of types is genuinely open/extensible at runtime (plugin architecture, external providers registering their own types, Spring-discovered beans)
- the registry is populated dynamically from configuration rather than a fixed, known-at-compile-time set of classes

Classification:

- Type = Evaluator-proposed
- Risk = MEDIUM
- Score Impact = NO for general absence
- Score Impact = Candidate when the closed set of types is confirmed known at compile time and the only safety net is a runtime exception
- Java Version = 17+ (sealed types + pattern matching for switch)

---

## Playbook Integration Notes

This skill is designed to run within the Iberia Engineering Copilot evaluation platform.

### Placement in the evaluation workflow

This skill executes AFTER:

1. MCP discovery (`searchStandards` + `readStandard`)
2. Architecture validation (§7–§8 of `initial_quality_evaluation.md`)
3. Design Quality analysis (§9 of `initial_quality_evaluation.md`)

This skill MUST NOT:

- run before MCP discovery is complete
- override or reinterpret MCP standards
- introduce new scoring rules
- affect architecture compliance conclusions
- classify findings as MCP-backed directly
- change scores directly

### Relevant playbook paths (for evaluator reference)

| Path | Relevance |
|------|-----------|
| `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` | Immutability (§7 MCP alignment candidate) |
| `docs/07-architecture-and-patterns/40-three-layer-architecture.md` | Three-layer architecture standard |
| `docs/05-testing-and-quality/jacoco.md` | Coverage thresholds (not directly used by this skill) |
| `docs/03-tech-stack/java-style-guide.md` | Optional usage alignment candidate (§5) |
| `docs/02-foundations/README.md` | Simplicity principle (general alignment) |

### execution-config.yml activation

To activate this skill, add to `execution-config.yml`:

```yaml
skills:
  - java-modern-practices
```

Mapped file: `docs/skills/java-modern-practices-skill.md`

---

## Hard Rules

- Do NOT affect scoring directly.
- MAY mark findings as `Candidate` when concrete inspected evidence shows maintainability, readability, type-safety, correctness, or runtime-risk impact.
- Only the main evaluation prompt may promote a `Candidate` finding into scoring impact.
- Do NOT present findings as Playbook violations unless the exact MCP standard was read and listed by the main prompt.
- Do NOT recommend Java features without context.
- Prefer domain clarity over language novelty.
- Findings must include concrete class/method evidence.
- For every skill rule executed, the evaluator MUST list non-compliant classes and explain why each class does not comply.
- Aggregate counts or heuristic counts are not enough without class-level non-compliance details.
- Candidate classes found by grep/static pattern MUST be inspected before being classified as non-compliant.
- If a candidate was not inspected, mark it as `Candidate / Unverified`.
- HIGH-risk Java Modern Practices findings MUST include concrete class/method evidence.
- The compliance list MUST include the recommendation and minimum Java version when relevant.
- If code was not inspected → mark as `Unverified`, set Confidence = LOW.
- Always populate `Java Version` — a Java 21 finding is not applicable to a Java 17 service.
- For VT findings (§6.2 and §6.3) → precondition gate in §6 must pass first.
- For JPA findings (§11) → precondition gate in §11 must pass first.
- For Record DTO candidates (§1) → apply API contract stability check before reporting.
- Absence of virtual threads, sealed classes, pattern matching, sequenced collections, or text blocks MUST NOT be reported as a finding by itself.
- Repeated missed opportunities with concrete evidence MAY be reported as `Candidate`, but still remain non-scoring until the main prompt validates them.