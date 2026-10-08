---
name: myvanitys-ai-product-registration
description: Implement or review the AI-assisted cosmetic product registration workflow in myvanitys-api, including authenticated image analysis, secure temporary storage, structured vision output, duplicate matching, image promotion, consent, effective image and color resolution, and custom-image replacement or removal. Use only for backend product-image and AI-registration work.
---

# MyVanitys AI Product Registration

Implement the product-image initiative without coupling application behavior to a storage or vision provider.

## Start here

1. Read `.agents/AGENTS.md`, `.agents/MEMORY.md`, `doc/ARCHITECTURE.md`, and `doc/decisions/0013-image-analysis-contract.md`.
2. Confirm that the current task explicitly supersedes or revises ADR 0013 before reintroducing image analysis. Record the replacement decision rather than leaving contradictory accepted guidance.
3. Use the API-first workflow for every public endpoint or schema change.
4. Read only the references needed for the task:
   - Image ingestion, R2 keys, promotion, replacement, deletion, or URL resolution: [references/image-security-and-storage.md](references/image-security-and-storage.md).
   - Vision output, category constraints, normalization, or duplicate matching: [references/analysis-and-normalization.md](references/analysis-and-normalization.md).
   - Error behavior, feature flags, test coverage, or rollout readiness: [references/failure-and-test-matrix.md](references/failure-and-test-matrix.md).

## Invariants

- Image analysis never creates or modifies a product, product-user association, or permanent image.
- User identity always comes from `AuthenticatedUserContext`.
- Treat decoded files and model output as untrusted input.
- Pass existing categories to the analyzer as a closed set and reject invented category identifiers.
- Never expose model confidence scores.
- Bind every temporary image reference to the authenticated user, expiry, and intended operation.
- Keep user images private and serve them only through short-lived presigned GET URLs.
- Promote a user image to a public reference only with explicit consent.
- Keep storage and provider SDK imports inside secondary adapters. Extend ArchUnit coverage when a concrete SDK is introduced.
- Match product identity by normalized name plus normalized brand.
- Resolve effective images as USER, then REFERENCE, then PLACEHOLDER.
- Resolve effective color as user-specific color, then product reference color.
- Design database/object-storage ordering with compensation or an idempotent retry state for partial failures.

## Architectural shape

- Put orchestration in application use cases.
- Define provider-neutral secondary ports such as image storage, image sanitization, and product image analysis at the inward-facing boundary used by the application.
- Implement R2, the selected vision provider, and fake/in-memory behavior as secondary adapters.
- Keep primary adapters limited to authentication context, multipart/request mapping, command construction, and response mapping.
- Use a fake analyzer and in-memory storage for ordinary unit tests and local development. Do not choose a real provider in this skill.

## V1 boundaries

Keep the flow synchronous and user-confirmed. Do not add named shades, PAO, size, expiry, barcode lookup, ingredients, native mobile behavior, or asynchronous analysis jobs unless a later contract explicitly expands the scope.

## Completion evidence

Report which invariant each change implements, the ports and adapters affected, storage ownership/lifecycle behavior, failure mappings, tests run, feature-flag behavior, and any provider, privacy, cost, or rollout decision still unresolved.
