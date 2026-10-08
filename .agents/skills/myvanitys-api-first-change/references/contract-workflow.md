# Contract Workflow

Use this checklist for any public endpoint, schema, validation, media type, authentication, or error-contract change.

## 1. Establish the authoritative source

From `myvanitys-api`:

```powershell
rg -n "myvanitys-api-spec.version|myvanitys-api-spec" pom.xml
git status --short
```

From the sibling `myvanitys-api-spec` checkout:

```powershell
git branch --show-current
git status --short
git log --oneline --decorate -20
rg -n "<version>|operationId:|imageReference|image-analysis" pom.xml src/main/resources/rest/openapi.yaml
```

Compare the backend's consumed version with the checkout version and content. A matching version string alone is not sufficient when snapshots may have been republished; also inspect the relevant operation and generated signatures.

If they differ:

1. Search local branches, tags, and history for the consumed contract.
2. Inspect the contract bundled in the consumed artifact when available.
3. Fetch remote history only when the task permits updating local Git refs.
4. If the producing source still cannot be identified, stop and report the mismatch. Do not retrofit a stale checkout and call it authoritative.

## 2. Design the contract change

For every operation, decide and encode:

- Stable `operationId` and tags.
- Path, query, header, and body inputs.
- Authentication requirements.
- Request media type and size/format constraints.
- Required versus optional fields and nullability.
- Response status codes and schemas.
- `application/problem+json` errors using the shared `ProblemDetail` schema.
- Examples that match their schemas.
- Compatibility impact for generated consumers.

Do not invent unresolved product semantics. Record the missing decision and stop when it changes the wire contract materially.

## 3. Generate and inspect

In `myvanitys-api-spec`, use the Maven wrapper for the platform:

```powershell
.\mvnw.cmd clean verify
```

Inspect generated sources under `target/generated-sources/openapi` to confirm:

- Delegate method name and parameter order.
- Generated request and response types.
- Required fields and validation annotations.
- Multipart representation.
- Nullable and collection behavior.
- Documented response types.

Generated files are inspection output, not edit targets.

## 4. Consume locally before publishing

When the requested change includes backend implementation, install the validated spec artifact locally and update the backend property to the intended version:

```powershell
.\mvnw.cmd clean install
```

Then compile `myvanitys-api` and let compiler errors reveal every delegate/model integration point. Never publish to GitHub Packages or deploy merely because a local build succeeds.

## 5. Implement the backend

- Override every newly supported delegate operation.
- Map generated models only in the primary adapter or its mapper.
- Build application commands/queries with user identity from `AuthenticatedUserContext`.
- Put business validation in domain/application code, not generated types.
- Map infrastructure/provider failures to documented application errors and `ProblemDetail` responses.
- Add controller tests proving request-to-command mapping and response status/body behavior.
- Add an integration test when generated routing, multipart binding, validation, or exception handling is material.

## 6. Drift checks

Before handoff, verify:

- No generated Java file was edited.
- No accepted request field is silently ignored.
- Runtime statuses are documented.
- Every documented success path has an implementation.
- JWT identity cannot be overridden by the body or path.
- The backend POM points to the intended artifact version.
- Temporary consumer warnings and ADRs reflect the actual deployed state.
