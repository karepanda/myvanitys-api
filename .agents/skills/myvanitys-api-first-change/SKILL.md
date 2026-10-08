---
name: myvanitys-api-first-change
description: Plan and implement public REST contract changes across myvanitys-api-spec and myvanitys-api. Use when adding or changing endpoints, request or response schemas, validation, ProblemDetail responses, generated delegates or models, or the consumed specification version. Do not use for internal refactors with no API impact.
---

# MyVanitys API-First Change

Change the public contract before implementing backend behavior, and keep the specification source, generated artifact, and backend consumer aligned.

## Required workflow

1. Read `.agents/AGENTS.md`, `.agents/MEMORY.md`, the backend `pom.xml`, and the relevant controller and tests.
2. Read [references/contract-workflow.md](references/contract-workflow.md).
3. Determine whether the request explicitly authorizes a public contract change. If it does not, stop and ask before editing the specification or changing the consumed artifact version.
4. Compare the API-spec version consumed by the backend with the version and Git state of the sibling `myvanitys-api-spec` checkout.
5. Identify the branch, tag, or commit that produced the consumed artifact. If the local checkout is stale and the authoritative source cannot be identified, stop instead of editing the wrong specification.
6. Change `myvanitys-api-spec/src/main/resources/rest/openapi.yaml`; never edit generated Java sources.
7. Generate and compile the specification project, inspect the generated delegate and models, and confirm that the wire contract matches the intended behavior.
8. Consume the validated artifact in the backend, then implement the primary adapter, mapping, application, domain, and secondary-adapter changes required by the contract.
9. Run focused contract/controller tests followed by the verification appropriate to the affected layers.

## Non-negotiable rules

- Treat `myvanitys-api-spec` as the public source of truth.
- Never edit `com.myvanitys.api.rest.v1.*` or `com.myvanitys.api.model.v1.*` generated classes.
- Do not treat a generated delegate default response as an implementation. A missing override can silently expose an HTTP 501.
- Ensure every accepted request field reaches an application command or is deliberately rejected; never silently ignore contract fields.
- Obtain user identity from the JWT-backed `AuthenticatedUserContext`, never from a request body.
- Keep documented responses, validation, authentication, media types, examples, and `ProblemDetail` mappings consistent with runtime behavior.
- Do not publish or deploy an artifact unless the user explicitly requests that external action.

## Current project gotcha

The backend and the sibling specification checkout have previously reported different snapshot versions. Always detect the versions dynamically. Do not encode `1.11.0-SNAPSHOT`, `1.7.0-SNAPSHOT`, or any other observed value as permanent truth.

## Completion evidence

Report the authoritative spec source used, contract changes, generated interfaces inspected, backend implementation changed, commands run, results, compatibility impact, and any publish/deploy step that remains intentionally undone.
