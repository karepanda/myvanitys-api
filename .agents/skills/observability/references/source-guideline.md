# Observability

**Skill scope:** Evaluate runtime observability and operational-robustness signals — structured logging, correlation/tracing, health/metrics, external-client resilience, and exception-handling architecture.
**Score impact:** Conditional — advisory by default; specific findings are promoted via centrally-defined `skill_override` rules in `docs/frameworks/api-quality-scoring-rules.yml` (gated by `applies_when.expected_architecture`).
**Architecture:** Architecture-agnostic. Both promoted rules are gated `["any"]` — observability and exception-handling architecture are not Hexagonal/Three-Layer/Vertical-Slice specific concerns.
**Evidence source:** `scripts/scan_observability.ts` → `observability--{date}.json`.

## Promoted (score-impacting) rules

| Rule ID | Subcriterion | Population | Finding | `base_cap` | `penalty_per_percent` |
|---|---|---|---|---|---|
| `OBS_EXTERNAL_CLIENT_RESILIENCE` | `code_quality/tooling_integration` | External REST-client classes | No circuit-breaker/retry/bulkhead/rate-limiter | 9.0 | 0.03 |
| `OBS_EXCEPTION_HANDLING_ARCHITECTURE` | `code_quality/code_standards` | Exception-handling `@ControllerAdvice` classes | Catch-all handler leaks `ex.getMessage()`/stacktrace into the response body | 9.0 | 0.04 |

The remaining observability sub-scans (`dependencies`, `configuration`, `logging_and_correlation`, `tracing_and_propagation`, `health_and_metrics`, and the presence-only portion of `error_observability`) remain **advisory** under `ADVISORY_OBSERVABILITY_FINDINGS` — they inform the narrative but do not cap scores.

## Rules

### external_client_resilience

**Evidence key:** `rules.external_client_resilience`
**Population (`candidate_classes_inspected`):** classes that build an outbound REST client — `RestClient.builder`, `WebClient.builder`, `new RestTemplate`, or `@FeignClient`.
**Finding (`non_compliant_classes_count`):** the candidate has neither a resilience4j annotation (`@CircuitBreaker`, `@Retry`, `@Bulkhead`, `@RateLimiter`) nor a matching `resilience4j.*` block in `src/main/resources` keyed (fuzzy) to the client name.
**Confidence:** MEDIUM — annotation/config detection is structural; a client resilient via a shared gateway/service-mesh at the platform layer would be a false positive, so the finding is a candidate for review rather than a hard defect.

**Playbook reference:** [80-circuit-breaker.md](https://backstage.iberia.com/docs/default/component/engineering-management.playbook/07-architecture-and-patterns/80-circuit-breaker/), [90-retry-pattern.md](https://backstage.iberia.com/docs/default/component/engineering-management.playbook/07-architecture-and-patterns/90-retry-pattern/).

### exception_handling_architecture

**Evidence key:** `rules.exception_handling_architecture`
**Population (`candidate_classes_inspected`):** classes that are exception-handling advice — annotated `@ControllerAdvice` / `@RestControllerAdvice` **and** containing at least one `@ExceptionHandler`. Response-body/context advices without `@ExceptionHandler` (e.g. `WarningResponseBodyAdvice`, `RequestContextAdviceAdapter`) are excluded so the population reflects only exception handlers. Comments are stripped before parsing so a Javadoc reference to `@ExceptionHandler(Exception.class)` is never counted as a real handler.

The scanner runs three sub-checks. Only sub-check A is score-impacting.

#### A. Catch-all information leak (SCORED — `non_compliant_classes_count`)

**Finding:** a catch-all handler — `@ExceptionHandler` for `Exception`, `Throwable`, or `RuntimeException` — copies `ex.getMessage()`, `ex.getLocalizedMessage()`, `ex.toString()`, or a stacktrace into the HTTP **response body** returned to the caller. Internal implementation detail (SQL fragments, class names, file paths, framework messages) leaks to the client.
**Confidence:** HIGH.

Precision safeguards (all validated against the real `bps-j` oracle):

- The leak check targets **only the catch-all handler**. A *specific* handler for the application's own exception (e.g. `handleApiException(ApiException ex)`) may legitimately return `ex.getMessage()` — that message is authored by the application, not leaked framework internals — so it is not flagged.
- **Logging the exception is not a leak.** `log.error(ex.getMessage(), ex)` and `log.error("...", ex)` are stripped before the check; only `ex.getMessage()` in the *returned response* counts.

#### B. Handler ordering (INFORMATIONAL — `ordering_observations`)

**Observation:** within one advice class a catch-all `@ExceptionHandler` is declared textually **before** a more specific handler. Convention places the catch-all last. Spring resolves handlers by exception specificity regardless of source order, so this is a readability/maintenance observation, **not** a functional bug — it is reported for the narrative and does **not** affect the score.

#### C. `@Order` disambiguation across advices (INFORMATIONAL — `order_annotation_observations`)

**Observation:** two or more **global** (unscoped — no `assignableTypes` / `basePackages` / `annotations`) exception-handling advice classes each declare a catch-all, and not every one carries an explicit `@Order`. Precedence between competing global catch-alls is then undefined. Reported for the narrative; does **not** affect the score. Controller-scoped advices (`@RestControllerAdvice(assignableTypes = XController.class)`) do not compete and are exempt.

**Playbook reference:** [100-exception-handling.md](https://backstage.iberia.com/docs/default/component/engineering-management.playbook/07-architecture-and-patterns/100-exception-handling/) — documents centralized exception translation and non-revealing error responses.

**Real oracle (`bps-j` — positive / compliant):**

- `GlobalExceptionHandler` — specific handlers first (`ApiException`, `NoResourceFound`, `NoHandlerFound`, `HttpRequestMethodNotSupported`), catch-all `@ExceptionHandler(Exception.class)` **last**, returning a generic `"Internal server error"` with no `getMessage()`. Its specific `handleApiException` *does* use `ex.getMessage()` — correctly **not** flagged (specific handler, application-owned message).
- `ZodValidationExceptionHandler` — `@Order(Ordered.HIGHEST_PRECEDENCE)`, only specific handlers (no catch-all).

Both are recognised as compliant: `candidates=2, leaks=0, ordering=0, order_ann=0`.

**Cross-service results (regenerated evidence):**

| Service | Architecture | Candidates | Catch-all leaks (scored) | Ordering obs. | `@Order` obs. |
|---|---|---|---|---|---|
| bps-j | hexagonal | 2 | 0 | 0 | 0 |
| offer-manager | hexagonal | 4 | 0 | 0 | 0 |
| offer-availability | hexagonal | 2 | 0 | 1 | 0 |
| airline-profile | hexagonal | 1 | 0 | 1 | 0 |
| crm-agencies | vertical-slice | 0 | 0 | 0 | 0 |
| ndc-api-adapter | hexagonal | 4 | 0 | 4 | 0 |
| product-catalog-resource-access | three-layer | 1 | 0 | 1 | 0 |
| commissions-api | hexagonal | 1 | 0 | 0 | 0 |

Every real service's catch-all handler delegates to a manager/mapper that produces a generic response (e.g. ndc-api-adapter's `genericExceptionManager.manage(ex)`, product-catalog's `errorHandler.handleTechnicalError(exception)`), so **no real leak exists across the estate today** — the scored count is 0 everywhere. The rule is a regression guard: it activates only when a future change routes `ex.getMessage()`/stacktrace into a catch-all response body. The ordering observations (ndc-api-adapter, offer-availability, airline-profile, product-catalog) flag catch-all-before-specific ordering — legitimate Spring code, surfaced for the narrative only.

**Known limitations:**

- **Inheritance is not resolved.** Handlers inherited from an abstract base (e.g. offer-manager's `CommonControllerAdvice`, which has no `@ControllerAdvice` of its own) are analysed only where the `@ExceptionHandler` is textually present. The base class is excluded from the population; concrete subclasses are inspected on their own declared handlers.
- **Detector broadening backlog:** annotation-scoped catch-all detection and per-response-field leak tracing are future improvements.

## Scoring

See `docs/frameworks/api-quality-scoring-rules.yml` → the centralized `skill_override` rules. Each uses:

```
effective_cap = base_cap - pct × penalty_per_percent
```

Where `pct = (finding_count / population) × 100`. When `population <= 0` or the eligible finding count is `0`, the rule returns no cap (not applied) — a service with zero findings is never penalised. `OBS_EXCEPTION_HANDLING_ARCHITECTURE` uses `penalty_per_percent: 0.04` (vs. `0.03` for `OBS_EXTERNAL_CLIENT_RESILIENCE`) because a catch-all information leak is a security/robustness concern, not a stylistic one.

## Cross-scanner overlap

- `OBS_EXCEPTION_HANDLING_ARCHITECTURE` (`code_quality/code_standards`) and `OBS_EXTERNAL_CLIENT_RESILIENCE` (`code_quality/tooling_integration`) target **different** subcriteria and do not overlap — neither joins a `no_double_penalty_groups` entry.
- Domain-layer exception vocabulary (throwing generic `IllegalArgumentException`/`IllegalStateException` in the domain) is a **different** concern owned by `domain-model-quality` (`domain_generic_exceptions`). This skill covers the **infrastructure boundary** (how the `@ControllerAdvice` responds), not the domain layer.

## See also

- `scripts/scan_observability.ts` — Scanner implementation (`scanExternalClientResilience`, `scanExceptionHandlingArchitecture`)
- `scripts/__tests__/scanner_quality_tests.ts` — Fixtures for resilience (Gap #3) and exception-handling architecture (Gap #12)
- `docs/frameworks/api-quality-scoring-rules.yml` — `OBS_EXTERNAL_CLIENT_RESILIENCE`, `OBS_EXCEPTION_HANDLING_ARCHITECTURE`, `ADVISORY_OBSERVABILITY_FINDINGS`
