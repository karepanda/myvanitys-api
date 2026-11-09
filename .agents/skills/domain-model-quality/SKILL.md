---
name: domain-model-quality
description: Implement or review domain-model quality in myvanitys-api across controllers, validators, application services, domain objects, persistence adapters, and domain exceptions. Use when changing business rules or assessing anemic models, misplaced logic, thick adapters, validation ownership, and domain vocabulary.
---

# Domain Model Quality

Keep business decisions and vocabulary in the domain or application layer that owns them, while keeping adapters focused on boundary concerns.

## Workflow

1. Read [the complete source guideline](references/source-guideline.md) before a broad domain review, or read the applicable rule for a focused change.
2. Confirm the repository's architecture and package role before applying architecture-specific checks.
3. Inspect concrete collaborators and method bodies before classifying thick controllers, hardcoded business classifications, transport-level business validation, anemic models, persistence-layer state transitions, or generic domain exceptions.
4. Distinguish legitimate data carriers, value objects, persistence translation, and technical validation from misplaced business behavior.
5. For reviews, cite exact files and methods and state when a rule is not applicable or evidence is incomplete.

## Repository adaptation

The bundled document was written for an external evaluation and scanner framework. Its score formulas, scanner paths, MCP/Playbook references, and report schema are conditional context, not repository requirements. Do not create absent evaluator infrastructure or present advisory heuristics as established project policy. Apply relevant rules to code changes and reviews using concrete evidence.

