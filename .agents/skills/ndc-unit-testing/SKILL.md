---
name: ndc-unit-testing
description: Apply the NDC/Offer JUnit 5 and Mockito testing guideline when explicitly requested or adopted for a test change. Use to review naming, Nested usage, mocking discipline, mocked primary inputs, verify-heavy tests, and test-structure smells; keep NDC-specific rules advisory outside an NDC/Offer project.
---

# NDC Unit Testing

Use the NDC/Offer testing rules as focused test-design guidance while respecting this repository's established conventions.

## Workflow

1. Read [the complete source guideline](references/source-guideline.md) before a guideline audit, or read the relevant rule for a focused test change.
2. Also inspect the production class, its direct collaborators, the nearest tests, and any repository-local testing skill or convention.
3. Mock external collaborators and ports; construct real domain values, commands, DTOs, results, and primary inputs unless a documented exception applies.
4. Assert observable results, state, exceptions, and meaningful side effects. Use interaction verification only when the interaction is the behavior under test.
5. Confirm every candidate in source before reporting it. Follow repository-wide scan requirements only when the user requests a full guideline evaluation.

## Repository adaptation

This guideline is NDC/Offer-specific and partly advisory. It does not silently replace `myvanitys-api` naming or test-structure conventions. When the two differ, preserve the local convention unless the user explicitly adopts the NDC rule. Treat external scoring, MCP/Playbook status, evaluator output formats, and `execution-config.yml` as provenance; do not create missing evaluator infrastructure or claim external compliance without the cited source.

