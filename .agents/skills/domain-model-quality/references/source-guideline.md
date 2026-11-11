# Domain Model Quality

**Skill scope:** Evaluate domain model quality across architectural boundaries.
**Score impact:** Conditional — advisory by default; promoted via centrally-defined `skill_override` rules in `docs/frameworks/api-quality-scoring-rules.yml` (gated by `expected_architecture`).
**Architecture:** Per-rule. `anemic_domain_models` is hexagonal-only. Others apply to all supported architectures.

## Applicability

| Rule | Hexagonal | Three-Layer | Vertical-Slice |
|---|---|---|---|
| `thick_input_adapters` | applicable | applicable | applicable |
| `validator_hardcoded_business_classification` | applicable | applicable | applicable |
| `transport_cross_field_business_validation` | applicable | applicable | applicable |
| `anemic_domain_models` | applicable | not applicable | not applicable |
| `business_logic_in_persistence_adapter` | applicable | applicable | applicable |
| `domain_generic_exceptions` | applicable | not applicable | not applicable |

Non-applicable rules emit `applicable: false` with `not_applicable_reason` — never zero-count findings.

## Rules

### thick_input_adapters

**Population:** `@RestController` / `@Controller` classes.
**Finding:** Controller injects >1 non-mapper business collaborator (service, use case, processor, validator, etc.).
**Confidence:** MEDIUM. Collaborator classification uses conservative heuristics with type metadata.

Lombok `@RequiredArgsConstructor` is recognized as dependency injection.

### validator_hardcoded_business_classification

**Population:** Three-entry union, deduplicated:

1. Classes whose source contains `implements ConstraintValidator\b` (JSR-303 bean validators).
2. Classes whose source contains `implements Validator\b` (Spring `Validator` implementors).
3. Files named `*Validator*.java` whose source contains `\bclass\s+\w*Validator\w*\b` (`@Component`-style validators named like validators but not implementing either interface).

Excluded: `domain/**` classes (domain-owned validation is legitimate). Interface declarations (`interface GenericValidator<T> extends Validator`) excluded via the `\bclass\s+` constraint.

**Blind spot fixed (2026-07-30):** Prior to this change, the population only checked entries 1 and 2 — structural detection of `ConstraintValidator`/`Validator` interface implementation. This missed 19 of 50 (38%) validator-named classes across 3 services: all 9 validators in `bps-j`, all 3 in `offer-manager`, and 3 of 38 in `offer-availability`. These are Spring `@Component` beans with `validate(...)` / `validate*(...)` methods that perform business-rule validation but don't implement the JSR-303/Spring Validator interfaces. The broadened population (entry 3) closes this gap.

**Validated across 5 services spanning all 3 architecture styles:**

| Service | Architecture | Candidates | Non-compliant | Pct% |
|---|---|---|---|---|
| bps-j | hexagonal | 9 | 0 | 0% |
| offer-manager | hexagonal | 3 | 0 | 0% |
| offer-availability | hexagonal | 34 | 1 | 2.9% |
| crm-agencies | vertical-slice | 6 | 2 | 33.3% |
| product-catalog-resource-access | three-layer | 1 | 0 | 0% |

**Finding:** Validator contains static hardcoded collections (List.of, Set.of, Arrays.asList) with ≥3 business literals used for classification.
**Confidence:** MEDIUM. Technical whitelists (MIME, HTTP, dates) are excluded.

### transport_cross_field_business_validation

**Population:** ConstraintValidator/Validator implementations.
**Finding:** Validator applies cross-field business rules (conditional logic + multi-property access + domain classification literals) on transport DTOs.
**Confidence:** LOW to MEDIUM (see `findings_by_confidence`). Legitimate transport validators (date, currency, uniqueness, format) are excluded.

### anemic_domain_models

**Population:** Eligible domain model classes under `domain/**` (excluding DTO/entity/persistence/generated/provider subpackages).
**Finding:** Domain class with no business methods is manipulated by external classes (in application/, core/, infrastructure/).
**Confidence:** LOW to MEDIUM depending on model classification.

Model classification:
- `value_object` — Java records, Lombok @Value, immutable carriers
- `data_carrier` — Lombok @Data, @Builder+@Getter
- `aggregate_entity_candidate` — Plain class without Lombok accessors
- `request_response_dto` — Excluded from inspection
- `persistence_model` — Excluded from inspection
- `domain_port_or_contract` — Excluded from inspection
- `unknown` — Should not occur; indicates classification gap

Lombok annotations are detected and reported. Lombok-generated accessors (getter/setter/with) do not count as behavioral methods.

### business_logic_in_persistence_adapter

**Population:** Files ending in `PersistenceAdapter.java` (all supported architectures).
**Finding:** Persistence adapter contains domain-state transition logic — a switch or if-comparison on a domain enum/state type (ending in `Status`/`State`/`Type`) with side effects (set*, save, repository) — instead of receiving the resolved outcome from the domain/application layer. Pure value-mapping switches (e.g. `switch(status) { case ACTIVE: return "A"; }`) are NOT flagged — the scanner requires side effects within the branching body to distinguish a legitimate translation from a business decision.
**Confidence:** LOW to MEDIUM depending on branch count and detection method.

**Example (bps-j):** `ReportWritePersistenceAdapter.applyStatusTransition()` compares `newStatus` against `ReportStatus.CLOSED` and stamps `closedAt` based on the outcome — a domain-level transition decision in the persistence layer. Recommended fix: move the transition logic to a domain service and let the adapter receive the already-resolved outcome.

### domain_generic_exceptions

**Population:** Two-entry union, deduplicated:

1. Production `.java` files under any `domain/**` path (hexagonal/clean-enriched services).
2. Files named `*Rule.java` or `*Checker.java` wherever they live (VSA/feature-first services where business-rule logic lives in flat capability folders without a `domain/` wrapper — e.g., `bps-j`'s `report/lossreason/{Code51Rule,Code73Rule,AndRule,OrRule,...}.java`, ~40 classes).

**Finding:** Domain or business-rule class throws `IllegalArgumentException` or `IllegalStateException` directly for business-rule violations instead of using a custom domain-specific exception (a subclass of a documented `DomainException` base class). This leaks Java implementation vocabulary into the domain/business layer and prevents centralized, type-safe exception translation at the infrastructure boundary.
**Confidence:** HIGH. Detection is mechanical — matches the regex `throw new (IllegalArgumentException|IllegalStateException)(` on source lines.

**Playbook reference:** [100-exception-handling.md](https://backstage.iberia.com/docs/default/component/engineering-management.playbook/07-architecture-and-patterns/100-exception-handling/) — documents a `DomainException` base class and the Translation Pattern for centralized exception handling.

**Real example (offer-manager — hexagonal, domain/ folder):**

| File | Line | Exception | Message |
|---|---|---|---|
| `domain/shared/offer/program/corporate/CorporateId.java` | 81 | `IllegalStateException` | `"CLID validation failed: " + this.id` |
| `domain/shared/offer/program/corporate/CorporateId.java` | 87 | `IllegalArgumentException` | `"Corporate ID cannot be null or blank"` |
| `domain/shared/offer/program/onbusiness/OnBusinessId.java` | 37 | `IllegalArgumentException` | `"OnBusiness ID cannot be null or blank"` |
| `domain/offerprice/model/availability/OfferId.java` | 35 | `IllegalArgumentException` | `"ID cannot be null"` |
| `domain/offerprice/model/availability/OfferId.java` | 39 | `IllegalArgumentException` | `"Invalid UUID format: " + value` |
| `domain/shared/WarningCode.java` | 20 | `IllegalArgumentException` | `"Invalid warning code format: " + value` |

These 7 domain classes (12 total throw statements) all validate business invariants but use JDK exception types. The service has a `DomainException` base class at `domain/shared/exception/DomainException.java`, with 20+ subclasses for specific business-rule violations — yet several value objects bypass this hierarchy entirely.

**Real example (bps-j — VSA/feature-first, no domain/ folder):** bps-j has 61 business-rule classes (`*Rule.java`/`*Checker.java`) under `report/lossreason/`. None throw generic exceptions — all use internal abstractions or rely on the rule engine's own error vocabulary. The broadened population correctly identifies these as business-rule classes worthy of inspection without false negatives from the `domain/` folder gate. However, 2 non-Rule/Checker utility classes in the same package (`DataSourceCache.java`, `AirportRegion.java`) DO throw generic exceptions — these are outside the naming-convention scope and are not detected.

**Negative case:** `domain/shared/offer/program/ProgramId` that throws `InvalidProgramIdException extends DomainException` — NOT flagged. The scanner only detects `IllegalArgumentException` and `IllegalStateException`; any custom exception (even a non-DomainException one) passes.

**Recommended fix:** Replace `throw new IllegalArgumentException("...")` / `throw new IllegalStateException("...")` with a domain-specific exception from the `domain/*/exception/` hierarchy (e.g., `InvalidCorporateIdException`, `InvalidOnBusinessIdException`), enabling type-safe translation by `@ControllerAdvice` and preserving domain vocabulary at the infrastructure boundary.

## Scoring

See `docs/frameworks/api-quality-scoring-rules.yml` → the centralized `skill_override` rules (each gated by `applies_when.expected_architecture`) for percentage-based caps. Each rule uses:

```
effective_cap = max(base_cap - pct × penalty_per_percent, floor)
```

Where `pct = (finding_count / population) × 100`.

## Cross-scanner overlap

- **MapStruct business logic**: NOT duplicated here. Covered by `mapping-boundaries` scanner (`business_logic_in_mapper` + `mapper_calling_services_or_repositories`).
- **Domain knowledge leak**: `DMQ_HARDCODED_VALIDATOR_CLASSIFICATION` and `DOMAIN_KNOWLEDGE_LEAK` share `validator_classification` no-double-penalty group.

## See also

- `scripts/scan_domain_model_quality.ts` — Scanner implementation
- `docs/frameworks/api-quality-scoring-rules.yml` — Static scoring rules
- `docs/skills/mapping-between-boundaries/SKILL.md` — Mapper quality skill (no duplicate MapStruct rule)
