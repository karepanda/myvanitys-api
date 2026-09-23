# 0013 — Image-analysis API contract

- **Status:** Accepted — removal pending
- **Decision date:** 2026-09-22
- **Ticket:** TICKET 13 — Product decision: `POST /products/image-analysis` published without implementation
- **Scope:** Documentation and decision record only. This ticket does not change any runtime behavior, remove the operation, or edit the OpenAPI contract.

## Context and verified behavior

The image-analysis operation is published but has no implementation:

- The consumed artifact `com.myvanitys:myvanitys-api-spec:1.11.0-SNAPSHOT` bundles the authoritative contract at `rest/openapi.yaml`. It defines `POST /products/image-analysis` (`openapi.yaml:827-906`) with `operationId: analyzeProductImage`, bearer authentication, a required `multipart/form-data` body with a required binary `image` field, and advertised responses `200` (`ProductImageAnalysisResult`), `400`, `401`, `429`, `502`, and `504`.
- The stale local `myvanitys-api-spec` checkout is on `main` at `1.7.0-SNAPSHOT` and does **not** contain the operation or `imageReference`. It is not the source that produced the consumed `1.11.0-SNAPSHOT` and must not be edited as a substitute for recovering and versioning the real source.
- `ProductController` implements several `ProductsApiDelegate` methods but does **not** override `analyzeProductImage`. The generated delegate default returns `new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED)`, so the live route answers **HTTP 501**.
- **HTTP 501 is not part of the advertised response contract.** The contract lists only `200`, `400`, `401`, `429`, `502`, `504`.
- The generated fallback returns a bare `ResponseEntity` with no body, so **no useful `ProblemDetail`** is produced for the caller.
- The generated `CreateProductRequest` model accepts an optional `imageReference` (`openapi.yaml:1020-1027`), but `ProductController.createProduct` reads only `name`, `brand`, `categoryId`, and `colorHex`. `CreateProductCommand` has no image-reference field, so a submitted `imageReference` is **silently ignored**.
- The contract for `imageReference` and for `ProductImageAnalysisResult.imageReference` promises a temporary private bucket with a 24-hour lifecycle and promotion into permanent public storage (`openapi.yaml:1191-1196`). No such storage exists.
- No implementation foundation was found in the repository: no image-analysis use case or port, no AI/vision provider adapter, no multipart validation, no temporary object storage, no image-promotion flow, no lifecycle cleanup, no rate limiter, no provider timeout/error mapping, and no image-analysis tests.
- No frontend consumer exists: the web client (`myvanitys-web`) contains no `image-analysis` or `imageReference` usage.
- `doc/API_OVERVIEW.md` already warns that the endpoint and `imageReference` are not available.

A published endpoint is a contract to consumers: it asserts a supported capability and enlarges the public/security surface. This one advertises a feature that has never worked, returns an undocumented status, and silently drops a request field. That is honest to describe as debt, not as a feature.

## Decision

- Do **not** implement image analysis in the near term.
- Remove `POST /products/image-analysis` from the next breaking API-spec release.
- Remove `ProductImageAnalysisResult` after confirming it has no remaining references (currently it is referenced only by this operation: `openapi.yaml:876` and its definition at `openapi.yaml:1185`).
- Remove `CreateProductRequest.imageReference`.
- Do **not** add a temporary controller implementation, a fake success response, or an override that merely returns another undocumented error, purely to preserve the dead endpoint.
- Do **not** leave the operation published indefinitely as "not available."
- Keep the current consumer warning until the removal is published and consumed by the API.
- Do **not** remove or change `ProductResponse.imageUrl` under this ticket; its future is a separate contract concern to be evaluated on its own.

## Compatibility and release plan

### Phase 1 — recover the authoritative specification source

- Locate the source branch/tag/commit that produced the consumed `1.11.0-SNAPSHOT` (the exact artifact bound into the API's `pom.xml`).
- Do **not** treat the local `1.7.0-SNAPSHOT` checkout as authoritative, and do not edit it as if it were the `1.11` source.
- Work on a dedicated non-`main` branch in `myvanitys-api-spec`.

### Phase 2 — optional deprecation release

- If the versioning/release workflow supports it, mark `POST /products/image-analysis` and `CreateProductRequest.imageReference` as `deprecated` in the current compatible contract line.
- State that the operation is unimplemented and currently resolves to the generated HTTP 501 fallback.
- Do not advertise a delivery date.
- This phase is optional because the endpoint has never worked; a deprecation window mainly helps any generated-client consumers that reference the method or model.

### Phase 3 — breaking contract removal

- Publish a versioned breaking API-spec release that removes:
  - the `/products/image-analysis` path,
  - `ProductImageAnalysisResult`, if it is unreferenced,
  - `CreateProductRequest.imageReference`.
- Treat this as a **breaking** contract change even though the endpoint never worked, because generated consumers may reference the method or the model and will fail to compile after regeneration.

### Phase 4 — backend convergence

- Update `myvanitys-api` to the newly published API-spec artifact.
- Confirm the generated `ProductsApiDelegate` no longer exposes `analyzeProductImage`.
- Confirm `CreateProductRequest` no longer exposes `imageReference`.
- Confirm `ProductImageAnalysisResult` is absent.
- Compile and run focused and complete tests.
- No `ProductController` implementation needs deletion, because none exists.
- Do not manually edit generated classes; they are produced from the spec.

### Phase 5 — documentation

- After the breaking contract and the backend dependency are deployed, remove the temporary "not available" warning from `API_OVERVIEW.md`.
- Record the removal in the release notes/changelog.
- Do not claim the endpoint is removed before the deployed API consumes the new specification.

## Security rationale

A future image-analysis feature must not be reintroduced without explicit design for:

- Maximum upload size.
- Content-Type allowlisting plus file-signature/magic-byte validation.
- Image dimension and decompression-bomb limits.
- Filename and metadata handling.
- Malware scanning where appropriate.
- Authentication and authorization.
- Per-user and global rate limits.
- Provider timeout, retry, and circuit-breaking policy.
- Cost controls and abuse monitoring.
- Privacy, retention, deletion, and third-party processing disclosure.
- Temporary object-storage isolation and lifecycle cleanup.
- Opaque, unguessable references bound to the authenticated user.
- Promotion from temporary to permanent storage.
- Error mapping and `ProblemDetail` responses.
- Observability without logging image content or secrets.
- Unit, adapter, security, and end-to-end tests.

## Reintroduction criteria

Image analysis may return only through a future feature ticket containing:

- Confirmed product owner priority.
- UX/client requirements.
- Threat model.
- Provider and data-processing decision.
- Storage and lifecycle design.
- Cost/rate-limit budget.
- API contract.
- Backend and frontend implementation plan.
- Test and rollout strategy.

## Rejected alternatives

- Implementing it immediately without requirements or infrastructure.
- Leaving it published indefinitely as unavailable.
- Adding a fake or hard-coded success response.
- Adding a controller override that merely returns another undocumented error.
- Continuing to accept and silently ignore `imageReference`.
- Editing generated Java sources directly.
- Editing the stale local `1.7.0-SNAPSHOT` contract as if it were the published source.

## Current-state warning

- This decision is **not** the contract removal itself. TICKET 13 records the decision and the release plan only.
- The currently consumed/published artifact (`1.11.0-SNAPSHOT`) still exposes `POST /products/image-analysis` and `CreateProductRequest.imageReference`.
- The current route still falls through to the generated HTTP 501 fallback with no useful `ProblemDetail` body.
- Clients must not integrate with the operation or send `imageReference`.
- Removal is complete only after the authoritative contract is updated, published, and consumed by the backend.
