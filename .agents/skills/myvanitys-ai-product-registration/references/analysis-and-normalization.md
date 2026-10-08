# Analysis and Normalization

Read this reference when implementing structured vision analysis, category selection, normalization, or duplicate detection.

## Analyzer input

The application use case supplies the analyzer port with:

- Sanitized image content or a provider-neutral image handle.
- The current category set loaded from the category repository.
- A strict structured-output schema for `name`, `brand`, `categoryId`, and `colorHex`.
- Correlation metadata that contains no credentials or unnecessary personal data.

Use stable category identifiers/codes in the closed set expected by the public contract. The adapter may translate the provider-neutral request into a provider schema, but the provider SDK type must not cross the port.

## Analyzer output is untrusted

After the provider returns:

1. Parse only the structured response shape.
2. Trim and normalize text without replacing the user's eventual ability to edit it.
3. Validate name and brand using the same domain rules used during save.
4. Accept `categoryId` only when it exists in the supplied category set and database.
5. Accept `colorHex` only when it matches the domain format; normalize its case consistently.
6. Mark missing or invalid suggestions in `unresolvedFields` rather than fabricating values.
7. Never return provider confidence scores, raw provider payloads, chain-of-thought, or internal prompts.

An analysis response is a suggestion. The save request remains the source of user-confirmed values.

## Duplicate matching

Product identity is normalized name plus normalized brand.

- Use one shared normalization policy for create-time uniqueness and analysis-time lookup.
- Define normalization explicitly in domain/application code and repository queries; do not rely on provider casing or whitespace.
- Return at most the contract-defined match representation.
- A match never auto-associates the product or overwrites its catalog fields.
- The user may add the matched product to their vanity or dismiss it and create a distinct product.

If normalization, database collation, or uniqueness semantics are unresolved, stop before adding a constraint or claiming deterministic duplicate detection.

## Provider adapter behavior

- Keep model name, API client, authentication, request schema, retries, and response parsing in the secondary adapter.
- Enforce a bounded timeout consistent with the public 504 behavior.
- Retry only failures that are safe, transient, and within the approved latency/cost budget.
- Map provider throttling, invalid output, unavailability, and timeout into application-level failures.
- Do not log the image, full extracted text, provider credentials, or raw responses containing personal information.

## Fake adapter

The fake analyzer used by local development and tests must be deterministic and configurable per scenario. It should model success, unresolved fields, invalid category output, throttling, provider failure, and timeout without pretending to be a real recognition system.
