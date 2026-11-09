---
name: domain-port-ownership
description: Detect application-layer services that resolve a domain concept (e.g. currency, geolocation, corporate profile) via an application-owned output port, then hand the result to a domain factory/constructor — instead of the domain owning that port directly, per the Iberia Hexagonal Architecture Playbook's Clean Architecture model (domain/port/, e.g. CurrencyConverter, UserRepository). NON-SCORING by default — findings may only affect evaluation score if the main evaluation prompt promotes them using MCP/Playbook evidence and explicit sub-criterion mapping. Applicable to hexagonal architecture with a Clean Architecture domain/application/infrastructure layering; not applicable when no domain/ package exists (simple hexagonal variant).
license: Apache-2.0
metadata:
  author: iberia-software-engineering
  version: "1.0"
  inspired_by: "Iberia Hexagonal Architecture Playbook, 'Project Structure (Clean Architecture)'; Implementing Domain-Driven Design (Vernon), Ch. 12 — Repositories"
compatibility: Java 21 LTS. Spring Boot backend services. Hexagonal architecture with a Clean Architecture domain/application/infrastructure layering only.
---

# Skill — Domain Port Ownership (v1.0)

## Status

Evaluator extension. NON-SCORING by default.

Unlike `mapping-between-boundaries`, the underlying standard for this skill **is** published
in the Playbook: `docs/07-architecture-and-patterns/50-hexagonal-architecture.md`, section
"Project Structure (Clean Architecture)", which defines `domain/port/` explicitly with
`UserRepository.java` and `CurrencyConverter.java` as examples of ports the domain owns.

That said, the same governance discipline as every other skill in this evaluator applies:

- Findings from this skill are `Evaluator-proposed` by default.
- Findings MUST NOT affect scoring directly.
- A finding MAY be promoted by the main evaluation prompt only when all conditions are met:
  1. Concrete class/method/file evidence is inspected (the port, the injecting class, the
     domain factory call receiving the resolved value).
  2. The exact MCP standard is read and listed in `MCP Standards Used`
     (`docs/07-architecture-and-patterns/50-hexagonal-architecture.md`).
  3. The finding maps to `layer_decoupling → dependency_inversion` in
     `api-quality-evaluation-framework.md`.
  4. When promoted, the finding is reported as a genuine gap against the published Playbook
     standard — the same framing `mapping-between-boundaries` uses for `business_logic_in_mapper`
     once promoted. This is not cosmetic: a domain concept resolved through an
     application-mediated port instead of a domain-owned port is a real architecture-knowledge
     gap on the same footing as boundary/mapper violations, not a stylistic preference.

General absence of domain-owned ports MUST NOT be penalized by itself. A service with zero
domain-owned ports may simply not yet have exercised this pattern — that is not, on its own,
evidence of a problem.

---

## MCP Discovery Result

| Topic | MCP Coverage | Relation to this skill |
|---|---|---|
| Domain ownership of output ports (repository/converter concepts) | COVERED — `docs/07-architecture-and-patterns/50-hexagonal-architecture.md`, section "Project Structure (Clean Architecture)" | Directly relevant; still requires main-prompt MCP validation citing the exact section before being MCP-backed |
| Application service orchestration boundaries | POTENTIALLY RELATED — same document, "Architecture Principles" | Requires main-prompt MCP validation |
| Repository pattern / Aggregate must not own its own persistence port | NOT COVERED by Playbook | Advisory only — sourced from Vernon Ch. 12, informs the Hard Rules exclusion for write/persistence ports below, never cite as Playbook |

**Governance note:**

"COVERED" above means the *concept* is documented in the Playbook. It does **not** mean any
individual finding from this skill is automatically MCP-backed. A finding from this skill can
only become MCP-backed if the main evaluation prompt:

1. reads the exact MCP standard,
2. lists the exact standard path and section in `MCP Standards Used`,
3. validates concrete class/method evidence,
4. maps the finding to `layer_decoupling → dependency_inversion`,
5. applies the no-double-penalty rule (see Overlap Consolidation below — a class already
   flagged by `ARCH_DOMAIN_KNOWLEDGE_LEAK` / Signal A must not also be scored here).

---

## Purpose

Evaluate whether application-layer services are resolving domain concepts through ports that
the domain itself should own, per the Playbook's Clean Architecture model.

This skill does three things:

1. **Detect** — which application services inject an output port, and whether the port
   resolves a domain concept (e.g. currency, geolocation, corporate eligibility) versus an
   operational/configuration concern (e.g. feature flags, tunable parameters).
2. **Assess** — whether the detected case is a genuine domain-port-ownership opportunity, or
   whether it overlaps with a `ARCH_DOMAIN_KNOWLEDGE_LEAK` (Signal A) finding instead (business
   decision/classification logic present between the port call and the domain construction —
   that is a different rule's territory, not this skill's).
3. **Distinguish read from write** — a port used to persist or save an already-decided
   aggregate (e.g. `PricedOfferSaver`) is explicitly OUT of scope. Per Vernon (Ch. 12), an
   Aggregate does not own or call its own persistence port; the application layer obtaining the
   aggregate, mutating it, and then calling `repository.save(aggregate)` is the correct pattern,
   not a finding.

Key principle: this skill reports gaps against an explicit, published Playbook pattern
(domain/port ownership). Once promoted through `skill_scoring_overrides`, these are treated
with the same weight as other layer-decoupling violations (e.g. `business_logic_in_mapper`) —
they surface a real architecture-knowledge gap on the development team's side, not merely a
stylistic preference.

---

## Scope

Inspect production code under `src/main/java/`.

Focus on:

- Application service / use case classes under `**/application/**/service/**`
  (package-based discovery — do NOT rely on class name suffixes; `[Capability]Service` naming
  is not exclusive to either layer, see `AGENTS.md` Naming Conventions note)
- Classes injecting a type imported from `**/application/**/port/output/**`
- The domain type(s) (`**/domain/**`) that the method under inspection ultimately constructs
- Persistence/write ports (`*Saver`, and generally any port called after domain mutation is
  already complete) — tracked separately as explicitly out of scope, not silently ignored

---

## Precondition Gate

This skill MUST only run if:

- `expected_architecture: hexagonal` in `execution-config.yml`
- A `domain/` package exists as a sibling of `application/` and `infrastructure/`

If no `domain/` package is detectable:

- Report as `Not Applicable`, with the reason: "this service likely follows the simple
  (non-Clean-Architecture) hexagonal variant, where application legitimately owns business
  logic; see AGENTS.md and the Playbook before treating this as a gap."
- Do NOT report zero findings as if the check ran and passed — an explicit `Not Applicable` is
  required so the main prompt and the reader both know the check did not apply, rather than
  inferring a clean result.

---

## Output Format

Add under `## Additional Quality Insights`:

```markdown
### Domain Port Ownership Findings
```

### Section A — Port Classification (always present)

```markdown
#### Output Ports Referenced by Application Services

| Port | Injected By | Classification | Reason | Notes |
|---|---|---|---|---|
| CurrencyLoader | CurrenciesService | Domain-concept candidate | Resolves a domain value (currency) | Feeds Currencies.of(...) |
| Parameters | PaymentTimeLimitService | Operational config (excluded) | Tunable margins, not a domain concept | Correctly application-owned |
| PricedOfferSaver | (orchestrating use case) | Write/persistence port (out of scope) | Called after aggregate mutation completes | Correct pattern per Vernon Ch. 12 |
```

Classification values:

- `Domain-concept candidate`
- `Operational config (excluded)`
- `Write/persistence port (out of scope)`
- `Unverified` — port referenced but the call site could not be inspected

### Section B — Opportunities (always present, even if empty)

```markdown
#### Domain Port Ownership Opportunities

| Class | Port | Domain Type Assembled | Evidence | Recommendation | Score Impact | Formalization Status |
|---|---|---|---|---|---|---|
```

If none found → explicitly state:

```text
No domain port ownership opportunities detected in inspected classes.
```

Never state "0 opportunities" without the explicit sentence above — a bare zero reads as
absence of analysis, not as a checked-and-clean result.

### Section C — Compliance Lists by Rule (mandatory for executed checks)

```markdown
#### Compliance List — Domain Port Ownership Opportunity

Inspection scope: <repository-wide | sampled | targeted>
Candidate classes inspected: <N>
Opportunities found: <N>

| Class | Opportunity? | Why / Why not | Evidence | Recommendation |
|---|---|---|---|---|
```

### Section D — evidence-detail heading (mandatory, exact format)

In `evidence-detail--{date}.md`, include a heading in this **exact** format — the
post-flight validator (`validate_final_outputs.ts`) matches it with a strict regex; any
deviation (different wording, missing parens, wrong count) fails validation:

```markdown
### domain-port-ownership/domain_port_ownership_opportunity (N unique item(s); official count: N)
```

Where `N` is `rules.domain_port_ownership_opportunity.non_compliant_classes_count` from the
scanner JSON, exactly. Follow with the full item list, same as the other scanners'
evidence-detail sections (java-modern-practices/records, etc.).

---

## Overlap Consolidation with Signal A (MANDATORY)

`scan_domain_port_ownership.ts` emits a `possible_domain_knowledge_leak_overlap` array: classes
where a domain type is assembled from a port AND structural decision logic (`switch`, chained
`else if`) is present in the same file. These classes are **not** counted as domain-port-ownership
opportunities.

For every class name in `possible_domain_knowledge_leak_overlap`:

- Do NOT list it under Section B of this skill.
- Cross-reference it explicitly: "`<ClassName>` shows decision logic between the port call and
  domain construction — see `ARCH_DOMAIN_KNOWLEDGE_LEAK` findings for this class instead."
- Do NOT count the same class toward both this skill's opportunity count and the
  `ARCH_DOMAIN_KNOWLEDGE_LEAK` non-compliance count when computing any aggregate summary.

This mirrors the no-double-penalty principle used for hotspot consolidation in
`mapping-between-boundaries`, applied here across two different skills/rules instead of within
one.

---

## Playbook Integration Notes

### Placement in the evaluation workflow

This skill executes AFTER:

1. MCP discovery (`searchStandards` + `readStandard`)
2. Architecture validation (confirms `domain/` package presence — see Precondition Gate)
3. `ARCH_DOMAIN_KNOWLEDGE_LEAK` (Signal A) evidence is available, so overlap consolidation
   above can reference it

This skill MUST NOT:

- run before the `domain/` package presence check is complete
- override or reinterpret the Playbook's Clean Architecture model
- introduce new scoring rules
- classify findings as MCP-backed directly
- change scores directly (that stays `compute_quality_scores.ts`'s responsibility via the
  `skill_scoring_overrides` bridge, once a service's config promotes this finding)

### Relevant playbook paths

| Path | Relevance |
|------|-----------|
| `docs/07-architecture-and-patterns/50-hexagonal-architecture.md` | Primary source — "Project Structure (Clean Architecture)" defines domain/port/ |
| `docs/03-tech-stack/java-style-guide.md` | General Java style; not a domain-ownership standard |

### AGENTS.md as an evidence source

If the evaluated service's own `AGENTS.md` documents a stricter convention than the Playbook
(e.g. "domain: Java stdlib, Lombok only", forbidding domain from owning any port), cite it
verbatim via `agents_md_ref` per `recommendation-schema.md` — do not silently defer to the
Playbook over the service's own documented convention. Flag the discrepancy explicitly so the
service's own `AGENTS.md` can be corrected, rather than treating the Playbook as automatically
overriding a documented team decision.

### Rule activation & scoring

Scoring promotion for this skill's findings is defined centrally in
`docs/frameworks/api-quality-scoring-rules.yml` (`skill_override` rules gated by
`applies_when.expected_architecture: [hexagonal]`).
The per-service `skills:` / `skill_scoring_overrides:` blocks in `execution-config.yml` were
removed by the Track B migration — do not re-add rule definitions there.

Mapped file: `docs/skills/domain-port-ownership.md`

Evidence source (scanner output): `scan_domain_port_ownership.ts` →
`docs/evaluations/<service>/domain-port-ownership--{execution_date}.json`

---

## Hard Rules

- Findings are `Evaluator-proposed` by default.
- Do NOT affect scoring directly.
- Do NOT report findings as `MCP-backed` in this skill's own output — only the main evaluation
  prompt may do so, after independently reading and citing the exact Playbook section. Once
  promoted through `skill_scoring_overrides`, the finding is a genuine architecture gap against
  a published standard, not a stylistic suggestion — report it with the same directness used
  for `business_logic_in_mapper` findings, not softened "opportunity" language.
- Do NOT flag write/persistence ports (`*Saver`, or any port called after aggregate mutation is
  complete) — this is a distinct, already-correct pattern (Vernon Ch. 12), not a finding.
- Do NOT flag operational-config ports (`Parameters`, `Config`, `Configuration`,
  `FeatureFlags`, `FeatureFlag`, `Properties`, `Toggle` — configurable via
  `--operational-config-exclude`, this is a default, not an exhaustive global list).
- Do NOT double-count a class already reported under `ARCH_DOMAIN_KNOWLEDGE_LEAK` — see Overlap
  Consolidation above.
- Do NOT run this skill if no `domain/` package exists in the repository — report
  `Not Applicable` with the stated reason instead of guessing.
- Findings MUST include concrete class/method/port evidence.
- Candidate classes found by pattern-matching MUST be inspected before being classified — if a
  candidate was not inspected, mark it `Unverified`, not silently dropped.

---

## Appendix — Quick Reference

### Domain-concept port naming patterns observed (non-exhaustive, do not hardcode as the only valid set)

`*Loader`, `*Finder`, `*Manager`, `*Fetcher`, or a bare domain-noun interface name (e.g.
`AirlineProfile`) have all been observed as domain-concept-resolving ports in real Iberia
services. Do NOT rely on an allowlist of suffixes to decide relevance — the scanner's default
behavior (treat everything in `port.output.*` as a candidate unless excluded) is deliberately
the inverse of a suffix allowlist, because suffix conventions vary by team and an allowlist
silently produces false negatives as new naming shows up.

### Why write/persistence ports are excluded

An Aggregate that calls its own repository/saver port to persist itself couples the domain
model to persistence lifecycle concerns and complicates testing the aggregate in isolation.
The correct pattern — confirmed in `PricedOfferAggregate.java` (offer-manager) — is: the
aggregate exposes pure mutation methods with no I/O, and the application-layer use case calls
`save(...)` after mutation completes. This is correct as-is; do not recommend "aggregate should
own its saver" under any circumstance.
