# Failure and Test Matrix

Read this reference when defining error behavior, feature flags, rollout checks, or test coverage for AI-assisted product registration.

## Contract failures

| Condition | Expected contract behavior | Required proof |
|---|---|---|
| Missing/malformed multipart image | 400 | Controller or binding test |
| File exceeds configured limit | 413 | Multipart/security integration test |
| Unsupported type or invalid signature | 415 | Sanitizer and controller tests |
| Missing/invalid JWT | 401 | Filter/controller integration test |
| Per-user/global rate limit exceeded | 429 | Rate-limit adapter/use-case test |
| Provider unavailable or invalid response | 502 | Adapter mapping and controller error test |
| Provider/application timeout | 504 | Timeout mapping test without sleeping |
| Expired/foreign temporary reference | Contract-defined client error | Ownership/expiry unit and integration tests |

Do not add a status that is absent from the authoritative contract. If feature-flag-off behavior is not specified, obtain that contract decision before implementation.

## Unit-test obligations

- Successful analysis returns suggestions, unresolved fields, an owned temporary reference, and an optional match.
- Analysis never calls product save, product-user save, or permanent-image promotion.
- The analyzer receives exactly the current allowed categories.
- Invented categories and malformed colors become unresolved or validation failures according to the contract.
- Duplicate lookup uses normalized name plus brand.
- Create/add validates temporary-reference ownership and expiry before copying.
- Consent false never writes `refs/`; consent true only promotes when the reference policy permits it.
- Effective image and color precedence cover every fallback.
- Replace/remove operations affect only the authenticated user's association.
- Partial failures execute the chosen compensation or idempotent retry behavior.

## Adapter and integration obligations

- Sanitizer tests use representative valid and invalid fixtures without retaining personal images.
- R2 adapter tests verify keys, access mode, presigning, copy/delete mapping, and provider failures without leaking signed URLs.
- Provider adapter tests verify structured parsing and error mapping at the HTTP boundary.
- Controller integration tests cover multipart binding, JWT identity, size/type errors, and `ProblemDetail`.
- Persistence integration tests cover new keys, per-user color, category codes, constraints, and effective-read queries.
- ArchUnit proves provider and storage SDKs are confined to secondary adapters.

## Rollout gates

1. Fake adapter works locally without external credentials.
2. Real-provider staging checks have approved privacy, retention, timeout, rate, and cost settings.
3. Backend feature flag defaults and environment configuration are documented.
4. Frontend and backend flags produce a usable manual-entry fallback.
5. Temporary-object lifecycle is configured and verified in the target R2 environment.
6. Logs, metrics, and alerts expose rates and failures without image content, extracted text, secrets, or presigned URLs.
7. Production enablement is an explicit deployment decision, not a side effect of merging code.
